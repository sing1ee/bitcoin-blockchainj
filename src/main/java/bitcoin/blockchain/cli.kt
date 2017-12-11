package bitcoin.blockchain

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default


class CliArg(parser: ArgParser) {

    val dbCmd by parser.flagging("-i", "--db", help = "db cmd").default(false)
    val blocksDir by parser.storing("-d", "--blocks-dir",
            help = "blocks dir")
}