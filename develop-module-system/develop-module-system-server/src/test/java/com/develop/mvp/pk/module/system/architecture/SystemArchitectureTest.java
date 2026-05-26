package com.develop.mvp.pk.module.system.architecture;

import com.develop.mvp.pk.framework.test.architecture.DevelopArchitectureRules;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemArchitectureTest {

    @Test
    @Disabled("Pre-existing domain repository DO dependencies (dict/logger/notice) — fix before re-enabling")
    void dddRuntimeUnitShouldRespectLayerBoundaries() {
        DevelopArchitectureRules.verifyDddRuntimeUnit("com.develop.mvp.pk.module.system.domain");
    }

    @Test
    @Disabled("Hexagonal-Lite full-package audit — enable as each aggregate is migrated")
    void hexagonalLiteLayerBoundariesShouldBeRespected() {
        DevelopArchitectureRules.verifyDddRuntimeUnitFull("com.develop.mvp.pk.module.system");
    }

    @Test
    void migratedLoggerSubdomainShouldRespectLayerBoundaries() {
        DevelopArchitectureRules.domainShouldStayPure()
                .check(DevelopArchitectureRules.importPackages("com.develop.mvp.pk.module.system.domain.logger"));
        DevelopArchitectureRules.applicationShouldNotDependOnEntryOrInfrastructure()
                .check(DevelopArchitectureRules.importPackages("com.develop.mvp.pk.module.system.application.logger"));
        DevelopArchitectureRules.adapterShouldNotDependOnEachOther()
                .check(DevelopArchitectureRules.importPackages("com.develop.mvp.pk.module.system.infrastructure.logger"));
        DevelopArchitectureRules.entryLayersShouldNotAccessPersistenceDirectly()
                .check(DevelopArchitectureRules.importPackages("com.develop.mvp.pk.module.system.controller.admin.logger"));
    }

    @Test
    void migratedSystemSubdomainsShouldExposeStandardDddSkeleton() {
        Path sourceRoot = mainSourceRoot();
        List<String> migratedSubdomains = List.of("auth", "dept", "dict", "logger", "mail", "member", "module", "notice", "notify", "oauth2", "permission", "sms", "social", "tenant", "user");
        List<String> requiredSuffixes = List.of(
                "domain/%s/model",
                "domain/%s/valueobject",
                "domain/%s/event",
                "domain/%s/service",
                "domain/%s/repository",
                "application/%s/command",
                "application/%s/query",
                "application/%s/dto",
                "application/%s/port/inbound",
                "application/%s/port/outbound",
                "application/%s/service",
                "infrastructure/%s/persistence",
                "infrastructure/%s/external",
                "infrastructure/%s/rpc",
                "infrastructure/%s/cache",
                "infrastructure/%s/messaging",
                "convert/%s"
        );

        for (String subdomain : migratedSubdomains) {
            for (String suffix : requiredSuffixes) {
                String requiredPackage = suffix.formatted(subdomain);
                assertTrue(Files.isDirectory(sourceRoot.resolve(requiredPackage)),
                        () -> "Missing system DDD skeleton package: " + requiredPackage);
            }
        }
    }

    @Test
    void migratedApplicationServicesShouldImplementInboundPorts() {
        assertImplementsInboundPort("permission", "MenuUseCase", "MenuApplicationService");
        assertImplementsInboundPort("permission", "PermissionUseCase", "PermissionApplicationService");
        assertImplementsInboundPort("auth", "AuthUseCase", "AuthApplicationService");
        assertImplementsInboundPort("dept", "DeptUseCase", "DeptApplicationService");
        assertImplementsInboundPort("dict", "DictUseCase", "DictApplicationService");
        assertImplementsInboundPort("logger", "LoggerUseCase", "LoggerApplicationService");
        assertImplementsInboundPort("mail", "MailUseCase", "MailApplicationService");
        assertImplementsInboundPort("member", "MemberUseCase", "MemberApplicationService");
        assertImplementsInboundPort("module", "SystemModuleUseCase", "SystemModuleApplicationService");
        assertImplementsInboundPort("notice", "NoticeUseCase", "NoticeApplicationService");
        assertImplementsInboundPort("notify", "NotifyUseCase", "NotifyApplicationService");
        assertImplementsInboundPort("oauth2", "OAuth2UseCase", "OAuth2ApplicationService");
        assertImplementsInboundPort("sms", "SmsUseCase", "SmsApplicationService");
        assertImplementsInboundPort("social", "SocialUseCase", "SocialApplicationService");
        assertImplementsInboundPort("tenant", "TenantUseCase", "TenantApplicationService");
        assertImplementsInboundPort("user", "UserUseCase", "UserApplicationService");
    }

    private void assertImplementsInboundPort(String subdomain, String portSimpleName, String serviceSimpleName) {
        String basePackage = "com.develop.mvp.pk.module.system.application." + subdomain;
        assertDoesNotThrow(() -> {
            Class<?> port = Class.forName(basePackage + ".port.inbound." + portSimpleName);
            Class<?> service = Class.forName(basePackage + ".service." + serviceSimpleName);
            assertTrue(port.isAssignableFrom(service), serviceSimpleName + " must implement " + portSimpleName);
        });
    }

    private Path mainSourceRoot() {
        Path workingDirectory = Path.of(System.getProperty("user.dir"));
        Path moduleSourceRoot = workingDirectory.resolve("src/main/java/com/develop/mvp/pk/module/system");
        if (Files.isDirectory(moduleSourceRoot)) {
            return moduleSourceRoot;
        }
        return workingDirectory.resolve("develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system");
    }
}
