package bitcoin.blockchain

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Block: Table("block") {

    val id = integer("id").autoIncrement().primaryKey()
    val hash = varchar("hash", 64).index(true)
    val prevBlockHash = varchar("prev_block_hash", 64).index()
    val merkleRoot = varchar("merkle_root", 64)
    val version = long("version")
    val time = long("time").index()
    val nonce = long("nonce")
    val difficultyTarget = long("difficulty_target")
    val blkFileName = varchar("blk_file_name", 64).index()
}

object Transaction: Table("tx") {

    val id = integer("id").autoIncrement().primaryKey()
    val blockHash = varchar("block_hash", 64).index()
    val txHash = varchar("tx_hash", 64).index()
    val version = long("version")
    val inputCount = integer("input_count")
    val outputCount = integer("output_count")
    val inputCoinValue = long("input_coin")
    val outputCoinValue = long("output_coin")
    val txFee = long("tx_fee")
    val isCoinbase = bool("is_coinbase")
    val updateTime = long("update_time").index()
    val isTimeLock = bool("is_time_lock")
    val lockTime = integer("lock_time").index()

}

object TxInput: Table("tx_input") {

    val id = integer("id").autoIncrement().primaryKey()
    val txHash = varchar("tx_hash", 64).index()
    val address = varchar("address", 58).index()
    val coinValue = long("coin")
}

object TxOutput: Table("tx_output") {

    val id = integer("id").autoIncrement().primaryKey()
    val txHash = varchar("tx_hash", 64).index()
    val address = varchar("address", 58).index()
    val coinValue = long("coin")
}

fun initDB() {

    Database.connect("jdbc:postgresql:bitcoin?user=postgres&password=123",
            driver = "org.postgresql.Driver")
    transaction {
        SchemaUtils.drop(Block, Transaction, TxInput, TxOutput)
        SchemaUtils.create(Block, Transaction, TxInput, TxOutput)

        println(Block.insert {
            it[hash] = "0000000000000000001a7260e60bd00c7823f7b80ed1ee2ab3c40bbc0c94ca73"
        } get Block.id)
    }
}