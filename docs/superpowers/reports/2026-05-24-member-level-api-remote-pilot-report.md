# Member Level API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` level API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberLevelApi` remains the stable contract name.
- `MemberLevelRemoteClient` owns Feign remote transport identity with `contextId = "memberLevelRemoteClient"`.
- Product and trade Feign scan configurations register `MemberLevelRemoteClient` for remote mode.
- Product and trade business consumers continue depending on `MemberLevelApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No `user` or `point` member API groups were migrated.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-product-server -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.

## Follow-Up

- Plan the `point` member API group separately instead of batching multiple groups.
- Define the concrete local adapter binding strategy before creating any `local` packages.
