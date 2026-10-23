package canteen;

public enum OrderStatus {
    PENDING,
    PREPARING,
    READY,
    SERVED,
    CANCELLED;

    public static OrderStatus fromString(String s) {
        if (s == null) return PENDING;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == READY || next == CANCELLED;
            case READY -> next == SERVED || next == CANCELLED;
            case SERVED, CANCELLED -> false;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case PENDING -> "⏳ Pending";
            case PREPARING -> "🍳 Preparing";
            case READY -> "✅ Ready";
            case SERVED -> "📦 Served";
            case CANCELLED -> "❌ Cancelled";
        };
    }
}