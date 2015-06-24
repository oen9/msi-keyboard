package pl.oen.msi.keyboard.exception;

public class DeviceNotFoundException extends RuntimeException {
    public DeviceNotFoundException(final short vendorId, final short productId) {
        super("DeviceNotFoundException{" +
                "vendorId=" + vendorId +
                ", productId=" + productId +
                '}');
    }
}
