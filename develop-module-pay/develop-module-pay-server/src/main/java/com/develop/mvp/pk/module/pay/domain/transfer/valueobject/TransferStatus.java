package com.develop.mvp.pk.module.pay.domain.transfer.valueobject;
// DDD 角色：转账状态值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class TransferStatus {
    public static final TransferStatus WAITING = new TransferStatus(0);
    public static final TransferStatus PROCESSING = new TransferStatus(5);
    public static final TransferStatus SUCCESS = new TransferStatus(10);
    public static final TransferStatus CLOSED = new TransferStatus(20);
    private final Integer value;
    public TransferStatus(Integer value) { this.value = Objects.requireNonNull(value); }
    public Integer value() { return value; }
    public boolean isWaiting() { return value == 0; }
    public boolean isProcessing() { return value == 5; }
    public boolean isSuccess() { return value == 10; }
    public boolean isClosed() { return value == 20; }
    public boolean isWaitingOrProcessing() { return isWaiting() || isProcessing(); }
    public boolean isTerminal() { return isSuccess() || isClosed(); }
    @Override public boolean equals(Object o) { return o instanceof TransferStatus s && value.equals(s.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return "TransferStatus(" + value + ")"; }
}
