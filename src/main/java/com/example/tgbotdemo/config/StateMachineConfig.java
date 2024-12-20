package com.example.tgbotdemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import java.util.EnumSet;

import com.example.tgbotdemo.domain.statemachine.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<ChatStates, String> {
    @Autowired
    private GuardsConfig guardsConfig;
    @Autowired
    private MainActionsConfig mainActionsConfig;
    @Autowired
    private AdminActionsConfig adminActionsConfig;

    @Override
    public void configure(StateMachineStateConfigurer<ChatStates, String> states)
            throws Exception {
        states
                .withStates()
                .initial(ChatStates.MAIN)
                .states(EnumSet.allOf(ChatStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ChatStates, String> transitions)
            throws Exception {
        transitions
                // From MAIN state
                .withInternal()
                .source(ChatStates.MAIN).event("/start").action(mainActionsConfig.start())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("сколько серебра у моей гильдии")
                .action(mainActionsConfig.getGuildMoney())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("сколько у меня серебра").action(mainActionsConfig.getUserMoney())
                .and()
                .withExternal()
                .source(ChatStates.MAIN).target(ChatStates.ORDER_ASKING_CELL).event("инвестировать в территорию")
                .action(mainActionsConfig.orderFirstStep()).guard(guardsConfig.isBlocked())
                .and()
                .withInternal()
                .source(ChatStates.MAIN).event("мои инвестиции в территории")
                .action(mainActionsConfig.getUserOrders())
                .and()
                .withExternal()
                .source(ChatStates.MAIN)
                .event("сколько гильдии инвестировали в территории").target(ChatStates.INFO_ASKING_CELL)
                .action(mainActionsConfig.getGuildOrders())
                .guard(guardsConfig.isBlocked())
                .and()
                .withInternal()
                .source(ChatStates.MAIN)
                .event("обзор карты").action(mainActionsConfig.getOverwiew()).guard(guardsConfig.isBlocked())
                .and()
                .withExternal()
                .source(ChatStates.MAIN).target(ChatStates.ADMIN).event("/admin").guard(guardsConfig.adminGuard())
                .action(adminActionsConfig.sendAdminMenu())
                // From INFO_ASKING_CELL state
                .and()
                .withExternal()
                .source(ChatStates.INFO_ASKING_CELL).target(ChatStates.MAIN).event("BACK_TO_MENU")
                // From ORDER_ASKING_CELL state
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_CELL).target(ChatStates.MAIN).event("BACK_TO_MENU")
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_CELL).target(ChatStates.ORDER_ASKING_AMOUNT)
                .event("NEXT").action(mainActionsConfig.orderSecondStep()).guard(guardsConfig.isBlocked())
                // From ORDER_ASKING_AMOUNT state
                .and()
                .withExternal()
                .source(ChatStates.ORDER_ASKING_AMOUNT).target(ChatStates.MAIN).event("BACK_TO_MENU")
                // From ADMIN
                .and()
                .withExternal()
                .source(ChatStates.ADMIN).target(ChatStates.MAIN).event("выйти в главное меню")
                .action(mainActionsConfig.sendMenu())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("добавить серебро пользователям")
                .action(adminActionsConfig.addMoneyToUsers())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("загрузить таблицу с пользователями")
                .action(adminActionsConfig.loadUsers())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("остановить вложения").action(adminActionsConfig.stopTrades())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("возобновить вложения").action(adminActionsConfig.restartTrades())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("закрепить клетки за гильдиями")
                .action(adminActionsConfig.fixWinners())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("загрузить карту")
                .action(adminActionsConfig.loadNewMap())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("очистить поле")
                .action(adminActionsConfig.clearCells())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("показать поле")
                .action(adminActionsConfig.showMap())
                .and()
                .withInternal()
                .source(ChatStates.ADMIN).event("выгрузить бд")
                .action(adminActionsConfig.uploadDB());

    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ChatStates, String> config) throws Exception {
        config.withConfiguration()
                .autoStartup(true);
    }

}
