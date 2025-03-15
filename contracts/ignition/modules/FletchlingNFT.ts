// This setup uses Hardhat Ignition to manage smart contract deployments.
// Learn more about it at https://hardhat.org/ignition

import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const ONE_GWEI: bigint = 1_000_000_000n;

const FletchlingNFTModule = buildModule("FletchlingNFTModule", (m) => {
  const fletchlingNFT = m.contract("FletchlingNFT", ["0x9a4269B534e63b99DE18587fA5528D23b8cE7f24", "0x95C9A7B68A44f148e3f54649ea0d38924507BdBD", "0x95C9A7B68A44f148e3f54649ea0d38924507BdBD"]);

  return { fletchlingNFT };
});

export default FletchlingNFTModule;

