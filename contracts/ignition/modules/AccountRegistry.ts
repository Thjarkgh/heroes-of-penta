// This setup uses Hardhat Ignition to manage smart contract deployments.
// Learn more about it at https://hardhat.org/ignition

import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const ONE_GWEI: bigint = 1_000_000_000n;

const AccountRegistryModule = buildModule("AccountRegistryModule", (m) => {
  const accountRegistry = m.contract("AccountRegistry");

  return { accountRegistry };
});

export default AccountRegistryModule;
