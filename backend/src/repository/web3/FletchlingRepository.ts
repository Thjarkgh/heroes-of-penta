import { BytesLike, encodeBytes32String, ethers } from "ethers"
import Wallet from "../../domain/entities/walletAggregate/Wallet";
import IFletchlingRepository from "../../service/IFletchlingRepository";
import Fletchling from "../../domain/entities/heroAggregate/Fletchling";


export default class FletchlingRepository implements IFletchlingRepository {
  private readonly contract: ethers.Contract;


  constructor(
    rpcUrl: string,
    contractAddress: string
  ) {
    const provider = new ethers.JsonRpcProvider(rpcUrl);
    const abi = [
      "error AccessControlBadConfirmation()",
      "error AccessControlUnauthorizedAccount(address account, bytes32 neededRole)",
      "error ERC2981InvalidDefaultRoyalty(uint256 numerator, uint256 denominator)",
      "error ERC2981InvalidDefaultRoyaltyReceiver(address receiver)",
      "error ERC2981InvalidTokenRoyalty(uint256 tokenId, uint256 numerator, uint256 denominator)",
      "error ERC2981InvalidTokenRoyaltyReceiver(uint256 tokenId, address receiver)",
      "error ERC721EnumerableForbiddenBatchMint()",
      "error ERC721IncorrectOwner(address sender, uint256 tokenId, address owner)",
      "error ERC721InsufficientApproval(address operator, uint256 tokenId)",
      "error ERC721InvalidApprover(address approver)",
      "error ERC721InvalidOperator(address operator)",
      "error ERC721InvalidOwner(address owner)",
      "error ERC721InvalidReceiver(address receiver)",
      "error ERC721InvalidSender(address sender)",
      "error ERC721NonexistentToken(uint256 tokenId)",
      "error ERC721OutOfBoundsIndex(address owner, uint256 index)",
      "error NotOwner()",
      "error TraitValueUnchanged()",
      "error UnknownTrat(bytes32)",

      "event Approval(address indexed owner, address indexed approved, uint256 tokenId)",
      "event ApprovalForAll(address indexed owner, address indexed operator, bool approved)",
      "event BatchMetadataUpdate(uint256 _fromTokenId, uint256 _toTokenId)",
      "event BurnedForLevelUp(uint256 tokenId)",
      "event ContractURIUpdated()",
      "event MetadataUpdate(uint256 _tokenId)",
      "event Minted(address indexed minter, uint256 tokenId, uint8 speciesId)",
      "event RoleAdminChanged(bytes32 indexed role, bytes32 indexed previousAdminRole, bytes32 indexed newAdminRole)",
      "event RoleGranted(bytes32 indexed role, address indexed account, address indexed sender)",
      "event RoleRevoked(bytes32 indexed role, address indexed account, address indexed sender)",
      "event TraitMetadataURIUpdated()",
      "event TraitUpdated(bytes32 indexed traitKey, uint256 tokenId, bytes32 traitValue)",
      "event TraitUpdatedList(bytes32 indexed traitKey, uint256[] TokenIds)",
      "event TraitUpdatedListUniformValue(bytes32 indexed traitKey, uint256[] TokenIds, bytes32 traitValue)",
      "event TraitUpdateRange(bytes32 indexed traitKey, uint256 fromTokenId, uint256 toTokenId)",
      "event TraitUpdateRangeUniformValue(bytes32 indexed traitKey, uint256 fromTokenId, uint256 toTokenId, bytes32 traitValue)",
      "event Transfer(address indexed from, address indexed to, uint256 tokenId)",
      "event XPAdded(uint256 indexed tokenId, uint256 amountAdded, uint256 newTotalXP)",
    
      "function DEFAULT_ADMIN_ROLE() view returns (bytes32)",
      "function NAME_KEY() view returns (bytes32)",
      "function TRAINER_ROLE() view returns (bytes32)",
      "function XP_DISTRIBUTOR_ROLE() view returns (bytes32)",
      "function XP_KEY() view returns (bytes32)",
      "function addSpeciesVariantMetadata(uint8, string)", // speciesId, cid
      "function addXP(uint256, uint256)", // tokenId, amount
      "function approve(address, uint256)", // to, tokenId
      "function balanceOf(address) view returns (uint256)", //owner
      "function burnForLevelUp(uint256, address)", // tokenId, ownerCheck
      "function contractURI() view  returns (string)",
      "function getApproved(uint256) view returns (address)", // tokenId
      "function getRoleAdmin(bytes32) view returns (bytes32)", // role
      "function getTraitMetadataURI() view returns (string)",
      "function getTraitValue(uint256, bytes32) view returns (bytes32)", // tokenId, traitKey
      "function getTraitValues(uint256, bytes32[]) view returns (bytes32[])", // tokenId, traitKeys
      "function grantRole(bytes32, address)", // role, account
      "function hasRole(bytes32, address) view returns (bool)", // role, account
      "function isApprovedForAll(address, address) view returns (bool)", // owner, operator
      "function mint() payable",
      "function mintPrice() view returns (uint256)",
      "function name() view returns (string)",
      "function ownerOf(uint256) view returns (address)", // tokenId
      "function renounceRole(bytes32, address)", // role, callerConfirmation
      "function revokeRole(bytes32, address)", // role, address
      "function royaltyInfo(uint256, uint256) view returns (address, uint256)", // tokenId, salesPrice
      "function safeTransferFrom(address, address, uint256)", // from, to, tokenId
      "function safeTransferFrom(address, address, uint256, bytes)", // from, to, tokenId, data
      "function setApprovalForAll(address, bool)", // operator, approved
      "function setContractURI(string)", // uri
      "function setMintPrice(uint256)", // newPrice
      "function setTrainer(address)", // newTrainer
      "function setTrait(uint256, bytes32, bytes32)", // tokenId, traitKey, value
      "function setTraitMetadataURI(string)", // uri
      "function setXPDistributor(address)", // newDistributor
      "function supportsInterface(bytes4) view returns (bool)", // interfaceId
      "function symbol() view returns (string)",
      "function tokenByIndex(uint256) view returns (uint256)", // index
      "function tokenOfOwnerByIndex(address, uint256) view returns (uint256)", // owner, index
      "function tokenURI(uint256) view returns (string)", // tokenId
      "function totalSupply() view returns (uint256)",
      "function transferFrom(address, address, uint256)", //from, to, tokenId
      "function updateSpeciesVariantMetadata(uint8, uint8, string)",
      "function withdraw()"
    ];

    // The Contract object
    this.contract = new ethers.Contract(contractAddress, abi, provider);
  }

  async getFletchlingsOfWallet(wallet: string) {
    const count = await this.contract.balanceOf(wallet);

    const result = [] as Fletchling[];
    for (let i = 0; i < count; ++i) {
      const fletchlingId: bigint = await this.contract.tokenOfOwnerByIndex(wallet, i);
      const fletchlingCid: string = await this.contract.tokenURI(fletchlingId);
      // TODO: Cache!
      const fletchlingCidUrl = `https://ipfs.io/ipfs/${fletchlingCid.substring(7)}`;
      const fletchlingInfoRaw = await fetch(fletchlingCidUrl);
      const fletchlingInfo: { description: string, image: string } = await fletchlingInfoRaw.json();
      const nameKey = await this.contract.NAME_KEY();
      const xpKey = await this.contract.XP_KEY();
      const dynValues: string[] = await this.contract.getTraitValues(fletchlingId, [nameKey, xpKey]);
      result.push({
        id: Number.parseInt(fletchlingId.toString(10), 10),
        name: dynValues[0] === "0x0000000000000000000000000000000000000000000000000000000000000000" ? "Nameless" : ethers.decodeBytes32String(dynValues[0]),
        description: fletchlingInfo.description,
        xp: Number.parseInt(dynValues[1], 16),
        imageUrl: fletchlingInfo.image
      });
    }

    return result;
  }
  
  async getFletchling(id: number): Promise<Fletchling> {
    const cid = await this.contract.tokenURI(id);
    const info = await (await fetch(cid)).json();
    const nameKey = await this.contract.NAME_KEY();
    const xpKey = await this.contract.XP_KEY();
    const dynValues = await this.contract.getTraitValues(id, [nameKey, xpKey]);
    return {
      id,
      name: dynValues[0],
      description: info.description,
      xp: dynValues[1],
      imageUrl: info.image
    };
  }
}