/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import org.bitcoinj.core.Context
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.BlockFileLoader
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors

class LibraryTest {
    @Test
    fun testSomeLibraryMethod() {
        val classUnderTest=Library()
        assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod())
    }

    @Test
    fun testBitcoinj() {
        val np = MainNetParams.get()
        val filt = setOf<String>("55a2ccf54b5a579f29f21c417e97e3f536b974772a25988551e5048316f79a08",
                "03588c53ab67f27a8e1d0aff65b9ac882ccaf1decde21bc1cad4a138ba7d2403",
                "825e309ef73be3f971f824e3c382f09a0171294afdbbdb94d39cf4d3699a9e33",
                "f48482cb959232646b23159516546206edc67bc6390e8339c146bb66b115bb90",
                "496cca4c744a17b1619c4c9648865c42259d570186e012e86c2b5505457adeb2",
                "b7abb807260a1bd37296e2ccdb20407454efb00cad1d828902ac45a71fbe579c",
                "e6986f8e5741144e7b2189b3c585cb345326d44b7b05e21d1ff6a1c3f79ac38f",
                "ea40ec1add57c821cbba4984e2a1c1e52e8bd5170f722a3b8699331a602d02ca")

        Context.getOrCreate(np);
        val blockChainList = Files.list(File("/Users/zhangcheng/Downloads/blktest").toPath()).filter {
            val name = it.toFile().name
            name.startsWith("blk") && name.endsWith(".dat")
        }.map {
            it.toFile()
        }.collect(Collectors.toList())
        val loader = BlockFileLoader(np, blockChainList)

        loader.forEach { blk ->
            println("blkHash: ${blk.hashAsString}")
            blk.transactions!!.forEach { tx ->
                if (!filt.contains(tx.hashAsString)) {
                    println("txHash: ${tx.hashAsString}")
                    tx.inputs.forEach { input ->
                        if (!input.isCoinBase) {
                            println("addr: ${input.scriptSig.getFromAddress(np)}")
                        }
                    }
                }
            }
        }
    }
}