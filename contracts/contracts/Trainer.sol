// // SPDX-License-Identifier: MIT
pragma solidity 0.8.23;

// import "@openzeppelin/contracts/access/AccessControl.sol";
// import "./FletchlingNFT.sol";

// // Trainer contract orchestrates the "level up" procedure:
// // 1) Checks if NFT has enough XP (>= xpForLevelUp).
// // 2) Burns the NFT via FletchlingNFT.burnForLevelUp (requires user approval).
// // 3) Mints a new NFT with 0 XP (or some XP) and possibly new metadata.

// contract Trainer is AccessControl {
//     bytes32 public constant GAME_SERVER_ROLE = keccak256("GAME_SERVER_ROLE");

//     FletchlingNFT public fletchling;

//     constructor(address fletchlingNFTAddress, address adminAddress, address gameServerAddress) {
//         fletchling = FletchlingNFT(fletchlingNFTAddress);
//         _setupRole(DEFAULT_ADMIN_ROLE, adminAddress);
//         _setupRole(GAME_SERVER_ROLE, gameServerAddress);

//         // Also, we want the Trainer contract to have TRAINER_ROLE in the NFT contract:
//         // This must be granted by the NFT's admin after deployment. For example:
//         // FletchlingNFT(fletchlingNFTAddress).grantRole(FletchlingNFT.TRAINER_ROLE(), address(this));
//         // You can do that in Hardhat scripts or after deployment.
//     }

//     // The main levelUp function. 
//     // The game server calls this after verifying off-chain logic.
//     function levelUp(uint256 oldTokenId, string calldata newCid) external onlyRole(GAME_SERVER_ROLE) {
//         // 1) Check if NFT has enough XP
//         require(fletchling.xp(oldTokenId) >= fletchling.xpForLevelUp(), "Not enough XP");

//         // 2) Retrieve the NFT's owner to pass into burnForLevelUp
//         address owner = fletchling.ownerOf(oldTokenId);

//         // 3) Burn the old NFT (ensures user has approved this contract)
//         fletchling.burnForLevelUp(oldTokenId, owner);

//         // 4) Mint a new NFT with the same species, xp = 0, but new picture or metadata
//         uint8 oldSpecies = fletchling.species(oldTokenId);

//         // In this example, new XP is set to 0. 
//         // If you want to preserve leftover XP (some games do partial cost?), you'd do so here
//         uint256 newXP = 0;

//         // We call mintLeveledUp on the NFT
//         // to create the new token with the same species (or you can change species if you want).
//         fletchling.mintLeveledUp(owner, oldSpecies, newXP, newCid);

//         // The next line is optional: you could emit an event for an off-chain indexer
//         emit LeveledUp(oldTokenId, owner);
//     }

//     // ============== EVENTS ==============
//     event LeveledUp(uint256 oldTokenId, address owner);
// }
