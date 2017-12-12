package bitcoin.blockchain

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object Block: Table("block") {

    val id = integer("id").autoIncrement().primaryKey()
    val hash = varchar("hash", 64).index()
    val prevBlockHash = varchar("prev_block_hash", 64).index().nullable()
    val merkleRoot = varchar("merkle_root", 64).nullable()
    val version = long("version").nullable()
    val time = long("time").index().nullable()
    val nonce = long("nonce").nullable()
    val difficultyTarget = long("difficulty_target").nullable()
    val blkFileName = varchar("blk_file_name", 64).index().nullable()
}

object Transaction: Table("tx") {

    val id = integer("id").autoIncrement().primaryKey()
    val blockHash = varchar("block_hash", 64).index()
    val txHash = varchar("tx_hash", 64).index()
    val version = long("version").nullable()
    val inputCount = integer("input_count").nullable()
    val outputCount = integer("output_count").nullable()
    val inputCoinValue = long("input_coin").nullable()
    val outputCoinValue = long("output_coin").nullable()
    val txFee = long("tx_fee").nullable()
    val isCoinbase = bool("is_coinbase").nullable()
    val updateTime = long("update_time").index().nullable()
    val isTimeLock = bool("is_time_lock").nullable()
    val lockTime = integer("lock_time").index().nullable()

}

object TxInput: Table("tx_input") {

    val id = integer("id").autoIncrement().primaryKey()
    val txHash = varchar("tx_hash", 64)
    val address = varchar("address", 58)
    val coinValue = long("coin").nullable()
}

object TxOutput: Table("tx_output") {

    val id = integer("id").autoIncrement().primaryKey()
    val txHash = varchar("tx_hash", 64)
    val address = varchar("address", 58)
    val coinValue = long("coin")
}


fun connectDB() {
    Database.connect("jdbc:postgresql:bitcoin?user=postgres&password=123",
            driver = "org.postgresql.Driver")
}


fun initDB() {
    connectDB()

    transaction {
        SchemaUtils.drop(Block, Transaction, TxInput, TxOutput)
        SchemaUtils.create(Block, Transaction, TxInput, TxOutput)

//        Block.batchInsert(mutableListOf
//        ("0000000000000000001a7260e60bd00c7823f7b80ed1ee2ab3c40bbc0c94ca73",
//                "0000000000000000001a7260e60bd00c7823f7b80ed1ee2ab3c40bbc0c94ca74"),
//                body = {
//                    this[Block.hash]=it
//                })
    }
}