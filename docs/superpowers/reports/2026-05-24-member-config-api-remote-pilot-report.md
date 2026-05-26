# Member Config API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` config API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberConfigApi` remains the stable contract name.
- `MemberConfigRemoteClient` owns Feign remote transport identity with `contextId = "memberConfigRemoteClient"`.
- Trade price services continue depending on `MemberConfigApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No other member API groups were migrated.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.

## Follow-Up

- Decide whether the next member API group should follow the same remote adapter split.
- Define the concrete local adapter binding strategy before creating any `local/` packages.
- Do not expand to all member API groups without explicit approval.
