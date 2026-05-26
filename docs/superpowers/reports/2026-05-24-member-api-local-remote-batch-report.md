# Member API Local/Remote Batch Report

## Scope

Audited and validated the member `level`, `point`, and `user` API contracts plus their remote adapters as Batch 1 of the full-project API local/remote refactor.

## Contract Status

| Contract | Stable API | Remote Adapter | Consumer Scan Status |
|---|---|---|---|
| level | `MemberLevelApi` has no Feign identity | `MemberLevelRemoteClient` extends `MemberLevelApi` | product/trade scan remote client |
| point | `MemberPointApi` has no Feign identity | `MemberPointRemoteClient` extends `MemberPointApi` | trade scans remote client |
| user | `MemberUserApi` has no Feign identity | `MemberUserRemoteClient` extends `MemberUserApi` | product/promotion/trade scan remote client |

## Decisions

- Kept business consumers injecting stable member API contracts.
- Kept remote transport identity in `remote/*RemoteClient` classes only.
- Did not create `local/` packages because this batch only validates the current local server implementation and remote Feign adapter split.
- Did not move DTOs, enums, messages, VO, DO, Mapper, or domain objects.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: PASS
- `mvn compile -pl develop-module-member/develop-module-member-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-product-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-promotion-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: PASS

## Next Batch

Proceed to Batch 2: `system + infra`. Start with a read-only audit of existing `remote/*RemoteClient` classes and remaining Feign scans that still point at stable API contracts such as `FileApi`, `ConfigApi`, `WebSocketSenderApi`, or framework `CommonApi` contracts.
