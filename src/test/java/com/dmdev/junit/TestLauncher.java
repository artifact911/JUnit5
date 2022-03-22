package com.dmdev.junit;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

public class TestLauncher {

    public static void main(String[] args) {
        var launcher = LauncherFactory.create(); // создали дефолтный лаунчер для запуска тестов (cmd)
//        launcher.registerLauncherDiscoveryListeners(); можем тут, а можем ниже

        var summaryGeneratingListener = new SummaryGeneratingListener(); // смотреть результат выполнения тестов
//        launcher.registerTestExecutionListeners(); можем тут, а можем ниже.
//        launcher.registerTestExecutionListeners(summaryGeneratingListener);

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
//                .selectors(DiscoverySelectors.selectClass(UserServiceTest.class)) // где искать тесты
                .selectors(DiscoverySelectors.selectPackage("com.dmdev.junit.service"))
//                .listeners()
                .build();

        launcher.execute(request, summaryGeneratingListener); // выполнит тесты

        try (var writer = new PrintWriter(System.out)){
            summaryGeneratingListener.getSummary().printTo(writer);
        }
    }
}
