package bitcoin.blockchain

import com.xenomachina.argparser.ArgParser
import org.bitcoinj.core.*
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BlockFileLoader
import org.jetbrains.exposed.sql.Slf4jSqlLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import java.text.SimpleDateFormat


class Main {

    class Tx {
        var blockHash: String? = null
        var txHash: String? = null
        var isCoinbase: Boolean? = false
    }

    companion object {

        @JvmStatic fun main(args: Array<String>) {

            val log = LoggerFactory.getLogger(Main::class.java)

            connectDB()
            val np: NetworkParameters = MainNetParams()

            fun batchInsert(blkBuf: MutableList<org.bitcoinj.core.Block>,
                            txBuf: MutableList<Tx>,
                            txInBuf: MutableList<TransactionInput>,
                            txOutBuf: MutableList<TransactionOutput>) {

                transaction {
                    logger.addLogger(Slf4jSqlLogger)

                    Block.batchInsert(blkBuf, body = {

                        this[Block.hash] = it.hashAsString
                        this[Block.difficultyTarget] = it.difficultyTarget
                        this[Block.merkleRoot] = it.merkleRoot.toString()
                        this[Block.nonce] = it.nonce
                        this[Block.prevBlockHash] = it.prevBlockHash.toString()
                        this[Block.time] = it.time.time
                        this[Block.version] = it.version
                    })
                    Transaction.batchInsert(txBuf, body = {

                        this[Transaction.blockHash] = it.blockHash!!
                        this[Transaction.txHash] = it.txHash!!
                        this[Transaction.isCoinbase] = it.isCoinbase

                    })

                    TxInput.batchInsert(txInBuf, body = {
                        this[TxInput.address] = try {
                            it.fromAddress.toBase58()
                        } catch (e: ScriptException) {
                            log.info("txHash: ${it.parentTransaction.hashAsString}", e)
                            "Unparsed address [0]"
                        }
                        this[TxInput.txHash] = it.parentTransaction.hashAsString

                    })

                    TxOutput.batchInsert(txOutBuf, body = {
                        this[TxOutput.address] = try {
                            it.scriptPubKey.getToAddress(np).toBase58()
                        } catch (e: ScriptException) {
                            log.info("txHash: ${it.parentTransaction!!.hashAsString}", e)
                            "Unparsed address [0]"
                        }
                        this[TxOutput.coinValue] = it.value.value
                        this[TxOutput.txHash] = it.parentTransaction!!.hashAsString
                    })
                }

                blkBuf.clear()
                txBuf.clear()
                txInBuf.clear()
                txOutBuf.clear()
            }

            val argParser = CliArg(ArgParser(args))
            if (argParser.dbCmd) {
                initDB()
                return
            }
            val blocksDir = argParser.blocksDir
            Context.getOrCreate(np);
            val blockChainList = Files.list(File(blocksDir).toPath()).filter {
                val name = it.toFile().name
                name.startsWith("blk") && name.endsWith(".dat")
            }.map {
                it.toFile()
            }.collect(Collectors.toList())
            val loader = BlockFileLoader(np, blockChainList)
            var cnt = 0


            val blkBuf = mutableListOf<org.bitcoinj.core.Block>()
            val txBuf = mutableListOf<Tx>()
            val txInBuf = mutableListOf<TransactionInput>()
            val txOutBuf = mutableListOf<TransactionOutput>()
            loader.forEach { blk ->
                cnt += 1
                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss E")
                blkBuf.add(blk)
                println("blk: ${blk.hash} AT ${df.format(blk.time)}")

                blk.transactions!!.forEach{
                    val tx = Tx()
                    tx.blockHash = blk.hashAsString
                    tx.txHash = it.hashAsString
                    tx.isCoinbase = it.isCoinBase
                    txBuf.add(tx)
                    it.inputs.forEach { input ->
                        if (!input.isCoinBase) {
                            txInBuf.add(input)
                        }
                    }
                    it.outputs.forEach {
                        txOutBuf.add(it)
                    }
                }

                // batch insert
                if (blkBuf.size % 100 == 0) {
                    batchInsert(blkBuf, txBuf, txInBuf, txOutBuf)
                }
            }
            batchInsert(blkBuf, txBuf, txInBuf, txOutBuf)
            println("total: $cnt")
        }
    }
}