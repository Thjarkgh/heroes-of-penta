import {
  time,
  loadFixture,
} from "@nomicfoundation/hardhat-toolbox/network-helpers";
import { anyValue } from "@nomicfoundation/hardhat-chai-matchers/withArgs";
import { expect } from "chai";
import hre from "hardhat";

describe("AccountRegistry", function () {
  // We define a fixture to reuse the same setup in every test.
  // We use loadFixture to run this setup once, snapshot that state,
  // and reset Hardhat Network to that snapshot in every test.
  async function deployFixture() {
    // Contracts are deployed using the first signer/account by default
    const [owner, otherAccount] = await hre.ethers.getSigners();

    const AccountRegistry = await hre.ethers.getContractFactory("AccountRegistry");
    const accountRegistry = await AccountRegistry.deploy();

    return { accountRegistry, owner, otherAccount };
  }

  describe("Register", function () {
    describe("Validations", function () {
      it("Should register if not registered yet (owner)", async function () {
        const { accountRegistry, owner } = await loadFixture(deployFixture);

        const accountId = 123;
        await accountRegistry.registerAccount(accountId);
        const result = await accountRegistry.getAccountWalletAddress(accountId);
        
        expect(result).to.equal(owner.address);
      });

      it("Should register if not registered yet (other account)", async function () {
        const { accountRegistry, otherAccount } = await loadFixture(deployFixture);

        const accountId = 123;
        await accountRegistry.connect(otherAccount).registerAccount(accountId);
        const result = await accountRegistry.connect(otherAccount).getAccountWalletAddress(accountId);
        
        expect(result).to.equal(otherAccount.address);
      });

      it("Should do nothing if called with unchanged arguments", async function () {
        const { accountRegistry, otherAccount } = await loadFixture(deployFixture);

        const accountId = 123;
        await accountRegistry.connect(otherAccount).registerAccount(accountId);
        const result = await accountRegistry.connect(otherAccount).getAccountWalletAddress(accountId);
        
        expect(result).to.equal(otherAccount.address);

        await accountRegistry.connect(otherAccount).registerAccount(accountId);
        const result2 = await accountRegistry.connect(otherAccount).getAccountWalletAddress(accountId);

        expect(result2).to.equal(result);
      });

      it("Should change do ...", async function () {
        // const { lock, unlockTime } = await loadFixture(
        //   deployFixture
        // );

        // // Transactions are sent using the first signer by default
        // await time.increaseTo(unlockTime);

        // await expect(lock.withdraw()).not.to.be.reverted;
      });
    });

    // describe("Events", function () {
    //   it("Should emit an event on withdrawals", async function () {
    //     const { lock, unlockTime, lockedAmount } = await loadFixture(
    //       deployFixture
    //     );

    //     await time.increaseTo(unlockTime);

    //     await expect(lock.withdraw())
    //       .to.emit(lock, "Withdrawal")
    //       .withArgs(lockedAmount, anyValue); // We accept any value as `when` arg
    //   });
    // });

    // describe("Transfers", function () {
    //   it("Should transfer the funds to the owner", async function () {
    //     const { lock, unlockTime, lockedAmount, owner } = await loadFixture(
    //       deployFixture
    //     );

    //     await time.increaseTo(unlockTime);

    //     await expect(lock.withdraw()).to.changeEtherBalances(
    //       [owner, lock],
    //       [lockedAmount, -lockedAmount]
    //     );
    //   });
    // });
  });
});
