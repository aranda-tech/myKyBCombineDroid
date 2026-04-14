package tech.aranda.myKyBCombineDroid.protocol

/**
 * JX-APP-A BLE Protocol for KY Blocks K96234
 *
 * Packet structure (14 bytes):
 * [0]    0xAC        - fixed header
 * [1]    mod1        - module ID byte 1 (auto-detected)
 * [2]    mod2        - module ID byte 2 (auto-detected)
 * [3]    0x01        - fixed
 * [4]    ch1         - left wheel  (0x01=fwd, 0x80=stop, 0xFF=rev)
 * [5]    ch2         - right wheel (0x01=fwd, 0x80=stop, 0xFF=rev)
 * [6-11] 0x80        - unused channels
 * [12]   checksum    - sum(bytes[0..11]) & 0xFF
 * [13]   0x35        - fixed footer
 */
class JXProtocol {
    var mod1: Byte = 0xBC.toByte()
    var mod2: Byte = 0xB5.toByte()

    /** Update module bytes from a received packet */
    fun updateModule(packet: ByteArray) {
        if (packet.size >= 3 && packet[0] == 0xAC.toByte()) {
            mod1 = packet[1]
            mod2 = packet[2]
        }
    }

    /** Convert joystick axis (-1.0 to +1.0) to protocol byte
     *  -1.0 → 0x01 (forward), 0.0 → 0x80 (stop), +1.0 → 0xFF (reverse)
     */
    private fun axisToByte(value: Double): Byte {
        val clamped = value.coerceIn(-1.0, 1.0)
        val result = 0x80 + (clamped * 127).toInt()
        return result.coerceIn(1, 255).toByte()
    }

    /** Build a drive command packet
     *  @param driveY -1.0=forward, +1.0=reverse (left joystick vertical)
     *  @param steerX -1.0=left, +1.0=right (right joystick horizontal)
     */
    fun buildDrivePacket(driveY: Double, steerX: Double): ByteArray {
        return if (Math.abs(driveY) < 0.05 && Math.abs(steerX) > 0.05) {
            // Pure steering — spin wheels in opposite directions
            buildPacket(
                ch1 = axisToByte(-steerX),
                ch2 = axisToByte(steerX)
            )
        } else {
            // Driving with optional steering mix
            var left = driveY
            var right = driveY
            if (steerX > 0) {
                right = (right + steerX).coerceIn(-1.0, 1.0)
            } else if (steerX < 0) {
                left = (left - Math.abs(steerX)).coerceIn(-1.0, 1.0)
            }
            buildPacket(ch1 = axisToByte(left), ch2 = axisToByte(right))
        }
    }

    /** Build raw packet with explicit channel bytes */
    fun buildPacket(ch1: Byte, ch2: Byte): ByteArray {
        val packet = ByteArray(14)
        packet[0] = 0xAC.toByte()
        packet[1] = mod1
        packet[2] = mod2
        packet[3] = 0x01.toByte()
        packet[4] = ch1
        packet[5] = ch2
        for (i in 6..11) packet[i] = 0x80.toByte()
        packet[12] = checksum(packet)
        packet[13] = 0x35.toByte()
        return packet
    }

    private fun checksum(packet: ByteArray): Byte {
        val sum = packet.slice(0 until 12).sumOf { it.toInt() and 0xFF }
        return (sum and 0xFF).toByte()
    }

    val idlePacket get() = buildPacket(0x80.toByte(), 0x80.toByte())
    val forwardPacket get() = buildPacket(0x01.toByte(), 0x01.toByte())
    val reversePacket get() = buildPacket(0xFF.toByte(), 0xFF.toByte())
    val spinRightPacket get() = buildPacket(0x01.toByte(), 0xFF.toByte())
    val spinLeftPacket get() = buildPacket(0xFF.toByte(), 0x01.toByte())
}
