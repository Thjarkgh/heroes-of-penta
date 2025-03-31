package com.heroesofpenta.data.web3

enum class Chains(
  val chainName: String,
  val chainNamespace: String,
  val chainReference: String,
  //@DrawableRes val icon: Int,
  val color: String,
  val methods: List<String>,
  val events: List<String>,
  val order: Int,
  val chainId: String = "$chainNamespace:$chainReference"
) {

  SCROLL(
    chainName = "Scroll",
    chainNamespace = Info.Eth.chain,
    chainReference = "534352",
    //icon = R.drawable.ic_ethereum,
    color = "#617de8",
    methods = Info.Eth.defaultMethods,
    events = Info.Eth.defaultEvents,
    order = 1
  ),

  SCROLL_SEPOLIA(
    chainName = "Scroll Sepolia",
    chainNamespace = Info.Eth.chain,
    chainReference = "534351",
    //icon = R.drawable.ic_polygon,
    color = "#8145e4",
    methods = Info.Eth.defaultMethods,
    events = Info.Eth.defaultEvents,
    order = 2
  );

  sealed class Info {
    abstract val chain: String
    abstract val defaultEvents: List<String>
    abstract val defaultMethods: List<String>

    object Eth : Info() {
      override val chain = "eip155"
      override val defaultEvents: List<String> = listOf("chainChanged", "accountsChanged")
      override val defaultMethods: List<String> = listOf("eth_sendTransaction", "personal_sign", "eth_sign", "eth_signTypedData")
    }

    object Cosmos : Info() {
      override val chain = "cosmos"
      override val defaultEvents: List<String> = listOf()
      override val defaultMethods: List<String> = listOf("cosmos_signDirect", "cosmos_signAmino")
    }

    object Solana : Info() {
      override val chain = "solana"
      override val defaultEvents: List<String> = listOf()
      override val defaultMethods: List<String> = listOf("solana_signMessage", "solana_signTransaction", "solana_signAndSendTransaction", "solana_signAllTransactions")
    }
  }
}