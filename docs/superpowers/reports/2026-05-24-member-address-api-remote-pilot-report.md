# Member Address API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` address API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberAddressApi` remains the stable contract name.
- `MemberAddressRemoteClient` owns Feign remote transport identity with `contextId = "memberAddressRemoteClient"`.
- Trade address consumers continue depending on `MemberAddressApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No `user`, `level`, or `point` member API groups were migrated.
- Existing `MemberAddressRespDTO.java` example-text diff was left out of this pilot because DTO changes are outside the address remote adapter scope.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.

## Follow-Up

- Plan the next member API group separately instead of batching multiple groups.
- Define the concrete local adapter binding strategy before creating any `local` packages.
