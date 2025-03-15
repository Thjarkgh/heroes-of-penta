// SPDX-License-Identifier: MIT
pragma solidity 0.8.23;

import "@openzeppelin/contracts/interfaces/IERC4906.sol";
// import "@openzeppelin/contracts/interfaces/IERC7496.sol";
// import "@openzeppelin/contracts/interfaces/IERC7572.sol";
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Enumerable.sol";
import "@openzeppelin/contracts/token/common/ERC2981.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "./interfaces/IERC7496.sol";
import "./interfaces/IERC7572.sol";
// If you prefer Ownable instead of AccessControl, you can import Ownable:
//import "@openzeppelin/contracts/access/Ownable.sol";

// FletchlingNFT: An ERC721 contract that represents base-phase NFTs which have XP, species, etc.
// XP can be incremented only by an authorized XP distributor. A trainer contract can burn tokens
// to level them up, provided the token has enough XP.

error NotOwner();
error NameTooLong();
error UnknownTrait(bytes32);

contract FletchlingNFT is ERC721Enumerable, ERC2981, IERC7496, IERC7572, IERC4906, AccessControl {
    // dynamic props:
    string private _contractURI;
    string private _traitMetadataURI;
    bytes32 public constant NAME_KEY = bytes32("name");
    bytes32 public constant XP_KEY = bytes32("xp");

    // Roles
    bytes32 public constant XP_DISTRIBUTOR_ROLE = keccak256("XP_DISTRIBUTOR_ROLE");
    bytes32 public constant TRAINER_ROLE = keccak256("TRAINER_ROLE");

    // Token ID counter
    uint256 private _tokenIds;

    // Internal Mappings
    // Mapping: tokenId => species (0..4)
    mapping(uint256 => uint8) private _species;
    mapping(uint256 => uint8) private _tokenCIDs;

    // Mapping: tokenId => XP
    mapping(uint256 => uint256) private _xp;
    mapping(uint256 => bytes32) private _names;

    // For random assignment of NFT metadata, we keep an array of IPFS CIDs for each species:
    // speciesPictureCIDs[0] = [ "ipfs://...", "ipfs://...", ... ]
    // speciesPictureCIDs[1] = [ "ipfs://...", ... ]
    // ...
    mapping(uint8 => string[]) private _speciesPictureCIDs;

    // Random nonce for pseudo-random selection
    uint256 private _randomNonce;

    uint256 public mintPrice = 0.001 ether;

    // Owner is the admin. If you prefer AccessControl only, then you can remove Ownable
    // and rely on the DEFAULT_ADMIN_ROLE. But let's keep it consistent with AccessControl:
    constructor(
        // uint256 xpForLevelUp_,
        address adminAddress,
        address xpDistributor,
        address trainer
    ) ERC721("Fletchling of Penta", "FOP") {
        // xpForLevelUp = xpForLevelUp_;

        // Setup roles
        _grantRole(DEFAULT_ADMIN_ROLE, adminAddress);
        _grantRole(XP_DISTRIBUTOR_ROLE, xpDistributor);
        _grantRole(TRAINER_ROLE, trainer);

        // Alternatively, the trainer role can be assigned later by the admin.
        _traitMetadataURI = "ipfs://bafkreidrgaanenvuppqwek7konyvwt24bsb2x63slrptdytmubjsx666ae";

        // Initialize species CIDs
        _speciesPictureCIDs[0].push("ipfs://bafkreibi4p37bfy2xlcncgcqbo5njwj665dan7tbrg5ls67vegdysojqea");
        _speciesPictureCIDs[0].push("ipfs://bafkreidvxhkhx5dretqxmumbk3alfxwe2yvraqz6kkistxvwdvojt2l4ty");
        _speciesPictureCIDs[1].push("ipfs://bafkreid6ltt245oxxifvbpn4hct5uzabr4dljblsbwrfj2yp7f4raailjq");
        _speciesPictureCIDs[1].push("ipfs://bafkreibjlv5irusig33odtj3xcjfionbvcjgf6ld75dv63hesodklsbdqi");
        _speciesPictureCIDs[2].push("ipfs://bafkreia2hokasxpof4b7agetcadtqozouexhsysuf2mrtrdq7rll2niopy");
        _speciesPictureCIDs[2].push("ipfs://bafkreifixdiert6zjcwbakvzrpvxpn2rre26nni3xjstvdojztqq5doreq");
        _speciesPictureCIDs[3].push("ipfs://bafkreiflofzavibsuxbfnr3wsodldtp34j6bqgu4re72lwe62ftvx6252i");
        _speciesPictureCIDs[3].push("ipfs://bafkreifiaq7kh3eyqwku4vjwp2hjbnu36kswfktcv57663cyjhbt27kgnm");
        _speciesPictureCIDs[4].push("ipfs://bafkreiawijh2r6v4zo24t6zowbked3pcbwxarzmtexlqvi6y6ni43bppdq");
        _speciesPictureCIDs[4].push("ipfs://bafkreidfxhatcqsztplfpadn623pw7c3kp4ifshsly4oksfvyptwjav3f4");

        _setDefaultRoyalty(xpDistributor, 500);
        emit TraitMetadataURIUpdated();
    }

    function setTrait(uint256 tokenId, bytes32 traitKey, bytes32 value) public virtual override {
        // Revert if the token doesn't exist.
        _requireOwned(tokenId);

        if (traitKey == NAME_KEY) {
            // Only allow the token owner to update its name
            if (ownerOf(tokenId) != msg.sender) revert NotOwner();
            
           _names[tokenId] = value;
        } else if (traitKey == XP_KEY) {
            require(hasRole(XP_DISTRIBUTOR_ROLE, msg.sender));
            uint256 newValue = uint256(value);
            require(newValue >= _xp[tokenId]);

            _xp[tokenId] = newValue;
        }
            
        // Emit an event with the update (according to ERC-7496)
        emit TraitUpdated(traitKey, tokenId, value);
    }

    function getTraitValue(uint256 tokenId, bytes32 traitKey)
        public
        view
        virtual
        override
        returns (bytes32 traitValue)
    {
        // Revert if the token doesn't exist.
        _requireOwned(tokenId);

        // Call the internal function to get the trait value.
        if (traitKey == NAME_KEY) {
            return _names[tokenId];
        }
        if (traitKey == XP_KEY) {
            return bytes32(_xp[tokenId]);
        }
        revert UnknownTrait(traitKey);
    }

    function getTraitValues(uint256 tokenId, bytes32[] calldata traitKeys)
        public
        view
        virtual
        override
        returns (bytes32[] memory traitValues)
    {
        // Revert if the token doesn't exist.
        _requireOwned(tokenId);

        // Allocate memory array with the same length as traitKeys
        traitValues = new bytes32[](traitKeys.length);
        
        for (uint i = 0; i < traitKeys.length; i++) {
            if (traitKeys[i] == NAME_KEY) {
                traitValues[i] = _names[tokenId];
            } else if (traitKeys[i] == XP_KEY) {
                traitValues[i] = bytes32(_xp[tokenId]);
            } else {
                revert UnknownTrait(traitKeys[i]);
            }
        }
    }
    // ============== ADMIN FUNCTIONS ==============

    // Set xpForLevelUp (callable only by the admin/timelock)
    // function setXpForLevelUp(uint256 newXpRequirement) external onlyRole(DEFAULT_ADMIN_ROLE) {
    //     xpForLevelUp = newXpRequirement;
    // }

    // If you want to update the XP distributor address
    function setXPDistributor(address newDistributor) external onlyRole(DEFAULT_ADMIN_ROLE) {
        _grantRole(XP_DISTRIBUTOR_ROLE, newDistributor);
    }

    function setTrainer(address newTrainer) external onlyRole(DEFAULT_ADMIN_ROLE) {
        _grantRole(TRAINER_ROLE, newTrainer);
    }

    function setMintPrice(uint256 newPrice) external onlyRole(DEFAULT_ADMIN_ROLE) {
        mintPrice = newPrice;
    }

    // Provide a way to add new IPFS CIDs for each species or update them
    // Only admin can add to the list. 
    // If you want to set the entire array in one call, you'd use a different approach.
    function addSpeciesVariantMetadata(uint8 speciesId, string memory cid) external onlyRole(DEFAULT_ADMIN_ROLE) {
        require(speciesId < 5, "Species out of range");
        _speciesPictureCIDs[speciesId].push(cid);
    }

    // If you want to replace an existing index
    function updateSpeciesVariantMetadata(uint8 speciesId, uint8 variantIdx, string memory cid) external onlyRole(DEFAULT_ADMIN_ROLE) {
        require(speciesId < 5, "Species out of range");
        require(variantIdx < _speciesPictureCIDs[speciesId].length, "Index out of range");
        _speciesPictureCIDs[speciesId][variantIdx] = cid;
        emit BatchMetadataUpdate(0, type(uint256).max);
    }

    function withdraw() public onlyRole(XP_DISTRIBUTOR_ROLE) {
        //require(msg.sender == owner(), "Only owner can withdraw");
        (bool success, ) = payable(msg.sender).call{value: address(this).balance}("");
        require(success, "Failed to withdraw");
    }

    function setTraitMetadataURI(string calldata uri) external onlyRole(DEFAULT_ADMIN_ROLE) {
        // Set the new metadata URI.
        _traitMetadataURI = uri;
        emit TraitMetadataURIUpdated();
    }

    function setContractURI(string calldata uri) external onlyRole(DEFAULT_ADMIN_ROLE) {
        _contractURI = uri;
        emit ContractURIUpdated();
    }

    // ============== XP DISTRIBUTION FUNCTIONS ==============

    // Add XP to a token, only by XP_DISTRIBUTOR_ROLE
    function addXP(uint256 tokenId, uint256 amount) external onlyRole(XP_DISTRIBUTOR_ROLE) {
        //require(_ownerOf(tokenId), "Token doesn't exist");
        _requireOwned(tokenId);
        _xp[tokenId] += amount;
        emit XPAdded(tokenId, amount, _xp[tokenId]);
    }

    // If you want to also allow decreasing or setting XP, you can add separate function(s).
    // function setXP(uint256 tokenId, uint256 newXP) external onlyRole(XP_DISTRIBUTOR_ROLE) {
    //     require(_ownerOf(tokenId), "Token doesn't exist");
    //     xp[tokenId] = newXP;
    //     emit XPSet(tokenId, newXP);
    // }

    // ============== MINTING ==============
    // Open mint - anyone can call
    function mint() external payable {
        require(msg.value >= mintPrice);
        // require(speciesId < 5, "Invalid species ID");

        uint8 speciesId = uint8((_randomNonce + block.timestamp) % 5);
        require(_speciesPictureCIDs[speciesId].length > 0);

        uint8 variantIdx = uint8((_randomNonce + block.timestamp) % _speciesPictureCIDs[speciesId].length);
        
        _randomNonce = (_randomNonce + 1) % 10000;
        uint256 newTokenId = _tokenIds;
        _tokenIds++;

        // Assign initial species and XP
        _species[newTokenId] = speciesId;
        _xp[newTokenId] = 0;
        _tokenCIDs[newTokenId] = variantIdx;

        _safeMint(msg.sender, newTokenId);
        emit Minted(msg.sender, newTokenId, speciesId);
    }

    // A specialized mint that the Trainer contract may call to create a "leveled up" NFT
    // after burning the old one. 
    // This is restricted to TRAINER_ROLE so that only the trainer can do it.
    // function mintLeveledUp(
    //     address to,
    //     uint8 speciesId,
    //     uint256 newXP,
    //     string memory newCid
    // ) external onlyRole(TRAINER_ROLE) returns (uint256) {
    //     require(speciesId < 5, "Invalid species ID");

    //     _tokenIds.increment();
    //     uint256 newTokenId = _tokenIds.current();

    //     species[newTokenId] = speciesId;
    //     xp[newTokenId] = newXP;

    //     // The trainer provides a new CID for the leveled-up NFT. 
    //     // If you want to randomize, you can do so here too.
    //     _tokenCIDs[newTokenId] = newCid;

    //     _safeMint(to, newTokenId);

    //     emit LeveledUpMint(to, newTokenId, speciesId, newXP);
    //     return newTokenId;
    // }

    // ============== BURNING FOR LEVEL UP ==============
    // This is called by the trainer to burn a token 
    // after verifying the user has approved the trainer for that token.
    function burnForLevelUp(uint256 tokenId, address ownerCheck) external onlyRole(TRAINER_ROLE) {
        require(ownerOf(tokenId) == ownerCheck, "Wrong owner");
        require(
            getApproved(tokenId) == msg.sender || 
            isApprovedForAll(ownerCheck, msg.sender),
            "Trainer not approved"
        );
        _burn(tokenId);
        // Clean up storage if you want (delete mappings)
        delete _xp[tokenId];
        delete _species[tokenId];
        delete _tokenCIDs[tokenId];
        emit BurnedForLevelUp(tokenId);
    }

    // ============== VIEW FUNCTIONS ==============
    function tokenURI(uint256 tokenId) public view override returns (string memory) {
        //require(_ownerOf(tokenId), "ERC721Metadata: URI query for nonexistent token");
        _requireOwned(tokenId);
        // Return the IPFS CID as the metadata or the direct image location. 
        // Usually, tokenURI is expected to return a link to a JSON metadata file.
        // For demonstration, we'll just return the stored CID. 
        // In a real setup, you might store a JSON file on IPFS that includes the image field, attributes, etc.
        return _speciesPictureCIDs[_species[tokenId]][_tokenCIDs[tokenId]];
    }

    function contractURI() public view returns (string memory) {
        return _contractURI;
    }

    function getTraitMetadataURI() external view returns (string memory uri) {
        return _traitMetadataURI;
    }

    // Helper to retrieve the stored CID for a token
    // function getTokenCID(uint256 tokenId) external view returns (string memory) {
    //     require(_ownerOf(tokenId), "Query for nonexistent token");
    //     return _tokenCIDs[tokenId];
    // }

    function supportsInterface(bytes4 interfaceId) public view virtual override(ERC721Enumerable, ERC2981, IERC165, AccessControl) returns (bool) {
        return type(IERC7496).interfaceId == interfaceId || type(IERC7572).interfaceId == interfaceId
        || type(IERC4906).interfaceId == interfaceId || type(IERC165).interfaceId == interfaceId
        || ERC721Enumerable.supportsInterface(interfaceId) || ERC2981.supportsInterface(interfaceId)
        || ERC721.supportsInterface(interfaceId) || AccessControl.supportsInterface(interfaceId);
    }

    // ============== EVENTS ==============
    event XPAdded(uint256 indexed tokenId, uint256 amountAdded, uint256 newTotalXP);
    // event XPSet(uint256 indexed tokenId, uint256 newXP);
    event Minted(address indexed minter, uint256 tokenId, uint8 speciesId);
    //event LeveledUpMint(address indexed to, uint256 tokenId, uint8 speciesId, uint256 newXP);
    event BurnedForLevelUp(uint256 tokenId);
}
