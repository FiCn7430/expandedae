package lu.kolja.expandedae.network;

import net.minecraftforge.network.NetworkDirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketInfo {

    /**
     * The direction the packet should be sent in
     */
    NetworkDirection value();
}