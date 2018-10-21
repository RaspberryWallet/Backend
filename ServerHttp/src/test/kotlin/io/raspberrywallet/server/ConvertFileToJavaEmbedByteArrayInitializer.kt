package io.raspberrywallet.server

import java.io.File

fun main(args: Array<String>) {
    File("RaspberryWallet.cer").readBytes().forEachIndexed { index, byte ->
        print("(byte) 0x%02X, %s".format(byte, if (index.rem(8) == 0) "\n" else ""))
    }
}
