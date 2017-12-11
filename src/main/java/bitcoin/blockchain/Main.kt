package bitcoin.blockchain

import com.xenomachina.argparser.ArgParser
import org.bitcoinj.core.*
import org.bitcoinj.core.Block
import org.bitcoinj.core.Transaction
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BlockFileLoader
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import java.text.SimpleDateFormat


class Main {

    companion object {

        @JvmStatic fun main(args: Array<String>) {

            fun batchInsert(blkBuf: MutableList<Block>,
                            txBuf: MutableList<Transaction>,
                            txInBuf: MutableList<TransactionInput>,
                            txOutBuf: MutableList<TransactionOutput>) {

                bitcoin.blockchain.Block.batchInsert(blkBuf, body = {

                })

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
            val np: NetworkParameters = MainNetParams()
            Context.getOrCreate(MainNetParams.get());
            val blockChainList = Files.list(File(blocksDir).toPath()).filter {
                val name = it.toFile().name
                println(name)
                name.startsWith("blk") && name.endsWith(".dat")
            }.map {
                it.toFile()
            }.collect(Collectors.toList())
            val loader = BlockFileLoader(np, blockChainList)
            var cnt = 0


            val blkBuf = mutableListOf<Block>()
            val txBuf = mutableListOf<Transaction>()
            val txInBuf = mutableListOf<TransactionInput>()
            val txOutBuf = mutableListOf<TransactionOutput>()
            loader.forEach {
                cnt += 1
                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss E")
                blkBuf.add(it)
                println("blk: ${it.hash} AT ${df.format(it.time)}")

                bitcoin.blockchain.Block.insert {

                }
                it.transactions!!.forEach{
                    println("tx: ${it.hash} coinbase: ${it.isCoinBase}")
                    txBuf.add(it)
                    it.inputs.forEach {
                        println(it.parentTransaction.hashAsString)
                        if (!it.isCoinBase) {

                            txInBuf.add(it)
                            val address = try {
                                it.fromAddress
                            } catch (e: ScriptException) {
                                e.printStackTrace()
                                null
                            }
                            println("input: $address")
                        }
                    }
                    it.outputs.forEach {

                        txOutBuf.add(it)
                        val address = try {
                            it.scriptPubKey.getToAddress(np)
                        } catch (e: ScriptException) {
                            e.printStackTrace()
                            null
                        }
                        println("ouput: $address amount: ${it.value}")
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