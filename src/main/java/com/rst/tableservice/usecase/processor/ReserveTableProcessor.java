package com.rst.tableservice.usecase.processor;

import com.rst.tableservice.core.model.TableCondition;
import com.rst.tableservice.core.model.TableStatusType;
import com.rst.tableservice.usecase.port.ReserveDatasourcePort;
import com.rst.tableservice.usecase.port.SanderPort;
import com.rst.tableservice.usecase.port.TableConditionDatasourcePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static java.time.ZoneOffset.UTC;


/*
 * Documentation
 *
 * Reserve table if time is less than 30 minutes
 *  and remove the reservation after 30 minutes if the table is not occupied.
 *
 * Key in REDIS "table.reserve" is a hash table with 'key' as table id
 *  and 'value' as SET of times in seconds when the table is reserved.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveTableProcessor {
    private final TableConditionDatasourcePort tableConditionPort;
    private final ReserveDatasourcePort reserveDatasourcePort;
    private final SanderPort sanderPort;


    /*
     * Лоигка работы:
     * 1. Получаем все зарезервированные столики
     * 2. Проверяем время резерва
     * 3. Если время резерва больше 30 минут и стол не поменял статус на OCCUPAID, то столик освобождается - тут неогбходимо продумать как лучше сделать, так как стол может быть зарезервирован несколько раз и в разное время с промежутком 120 минут (по условию задачи)
     * 4. Если время резерва больше 30 минут и стол поменял статус на  OCCUPAID, то столик не освобождается - тут неогбходимо продумать как лучше сделать, так как стол может быть зарезервирован несколько раз и в разное время с промежутком 120 минут (по условию задачи)
     * 5. Если время резерва меньше 29 минут  то сол меняет флаг  что он занят на  true  (за 30 минут до резервирования стол меняет статус на OCCUPAID)
     * Дополнительно:
     *  необходимо добавить рассылку сообщений в MQ для резервирования столика, если столик не занят
     *
     * */

    @Scheduled(fixedDelay = 1000)
    public void execute() {
        log.info("process check reserved table is started {}", new Date());
        reserveDatasourcePort.getAllReservedTable().forEach(reservedTable -> {
            val reservedTime = reservedTable.reservedTimes();
            val reservedSize = reservedTime.size();
            reservedTime.forEach(time -> {
                val triggerTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), UTC);
                if (triggerTime.isAfter(LocalDateTime.now().plusMinutes(30))) {
                    removeReservation(tableConditionPort.getTableConditionByTableId(reservedTable.tableId()).get(), triggerTime, reservedSize == 1);
                    log.info("table {} was unreserved", reservedTable);
                } else if (triggerTime.isAfter(LocalDateTime.now().minusMinutes(30)) && triggerTime.isBefore(LocalDateTime.now().plusMinutes(30))) {
                    tableConditionPort.updateStatus(TableStatusType.OCCUPIED, reservedTable.tableId());
                    sanderPort.send("table.reserve", reservedTable.tableId(), "table is occupied");
                    log.info("table {} is occupied", reservedTable);
                }
            });
        });
    }


    private void removeReservation(TableCondition tableCondition, LocalDateTime reservationTime, boolean isLastReservation) {
        long tableId = tableCondition.getTableId();
        tableCondition.setStatus(isLastReservation ?
                TableStatusType.AVAILABLE
                : TableStatusType.RESERVED);

        tableCondition.setOccupied(false);
        reserveDatasourcePort.deleteReservedTime(tableId, reservationTime);
        sanderPort.send("table.reserve", tableId, "table is unreserved");
    }
}
