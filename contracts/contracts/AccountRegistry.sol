// SPDX-License-Identifier: MIT
pragma solidity 0.8.23;

contract AccountRegistry {
    
    // Stores which address is assigned to a given account-id
    struct AccountInfo {
        address walletAddress;
        uint lastUpdated;
    }
    mapping(uint => AccountInfo) private accountIdToAccountInfo;

    // Tracks, for each address, which account-id they have claimed 
    // and the timestamp of the last assignment
    struct WalletAddressInfo {
        uint accountId;
        uint lastUpdated;
    }
    mapping(address => WalletAddressInfo) private addressToWalletInfo;

    // One week in seconds
    uint constant ONE_WEEK = 7 * 24 * 60 * 60;

    /**
     * @dev Assigns `accountId` to the caller (msg.sender) with the following rules:
     *      1) If the caller already has a accountId assigned:
     *         a) If the assigned accountId == `accountId`, do nothing.
     *         b) Else, if more than a week has passed since last assignment,
     *            remove the old entry and add the new one.
     *         c) Otherwise, revert (force the caller to wait).
     *      2) If the caller has no accountId assigned, simply assign `accountId`.
     */
    function registerAccount(uint accountId) external {
        WalletAddressInfo storage wallet = addressToWalletInfo[msg.sender];
        AccountInfo storage account = accountIdToAccountInfo[accountId];

        // If it's the same number, do nothing
        if (wallet.accountId == accountId) {
            require(
                account.walletAddress == msg.sender,
                "If the current wallets assigned account is the provided accountId, then the account of the provided accountId must be assigned to the current wallet"
            );
            return;
        }

        // First verify that neither the wallet nor the account are on cooldown
        require(
            block.timestamp >= wallet.lastUpdated + ONE_WEEK,
            "Must wait at least one week to change your assigned account"
        );
        require(
            block.timestamp >= account.lastUpdated + ONE_WEEK,
            "Must wait at least one week to change assigned wallet"
        );

        // Clear out the old number -> address mapping
        accountIdToAccountInfo[wallet.accountId].walletAddress = address(0);
        accountIdToAccountInfo[wallet.accountId].lastUpdated = block.timestamp;

        if (account.walletAddress != address(0)) {
            addressToWalletInfo[account.walletAddress].accountId = 0;
            addressToWalletInfo[account.walletAddress].lastUpdated = block.timestamp;
        }

        // Assign the new number to this address
        wallet.accountId = accountId;
        wallet.lastUpdated = block.timestamp;
        account.walletAddress = msg.sender;
        account.lastUpdated = block.timestamp;
    }

    /**
     * @dev Returns the wallet address currently assigned to a given accountIdy.
     */
    function getAccountWalletAddress(uint accountId) external view returns (address) {
        return accountIdToAccountInfo[accountId].walletAddress;
    }

    function getAccountCooldown(uint accountId) external view returns (uint) {
        if (block.timestamp >= accountIdToAccountInfo[accountId].lastUpdated + ONE_WEEK) {
            return 0;
        } else {
            return accountIdToAccountInfo[accountId].lastUpdated + ONE_WEEK - block.timestamp;
        }
    }

    function getWalletCooldown() external view returns (uint) {
        if (block.timestamp >= addressToWalletInfo[msg.sender].lastUpdated + ONE_WEEK) {
            return 0;
        } else {
            return addressToWalletInfo[msg.sender].lastUpdated + ONE_WEEK - block.timestamp;
        }
    }
}
