
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class BankAccount {

    private final int accountId;
    private double balance;
    private final Lock lock = new ReentrantLock();
    private static final Logger logger = Logger.getLogger(BankAccount.class.getName());

    public BankAccount(int accountId, double balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public void deposit(double amount) {
        lock.lock();
        try {
            balance += amount;
            String logMessage = "Deposited " + amount + " into Account " + accountId + ". New Balance: " + balance;
            System.out.println(logMessage);
            logger.info(logMessage);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(double amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                String logMessage = "Withdrew " + amount + " from Account " + accountId + ". New Balance: " + balance;
                System.out.println(logMessage);
                logger.info(logMessage);
            } else {
                String logMessage = "Insufficient funds for withdrawal from Account " + accountId;
                System.out.println(logMessage);
                logger.warning(logMessage);
            }
        } finally {
            lock.unlock();
        }
    }

    public void transfer(BankAccount toAccount, double amount) {
        if (this == toAccount) {
            System.out.println("Cannot transfer to the same account!");
            return;
        }

        boolean success = false;
        while (!success) {
            if (this.lock.tryLock()) {
                try {
                    if (toAccount.lock.tryLock()) {
                        try {
                            if (balance >= amount) {
                                this.balance -= amount;
                                toAccount.balance += amount;
                                String logMessage = "Transferred " + amount + " from Account " + accountId
                                        + " to Account " + toAccount.accountId;
                                System.out.println(logMessage);
                                logger.info(logMessage);
                            } else {
                                String logMessage = "Insufficient funds to transfer from Account " + accountId;
                                System.out.println(logMessage);
                                logger.warning(logMessage);
                            }
                            success = true;
                        } finally {
                            toAccount.lock.unlock();
                        }
                    }
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    public double getBalance() {
        return balance;
    }

    public int getAccountId() {
        return accountId;
    }
}

class CustomerThread extends Thread {

    private final BankAccount fromAccount;
    private final List<BankAccount> allAccounts;
    private final Scanner scanner;

    public CustomerThread(BankAccount fromAccount, List<BankAccount> allAccounts, Scanner scanner) {
        this.fromAccount = fromAccount;
        this.allAccounts = allAccounts;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Choose an action for Account " + fromAccount.getAccountId() + ":");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Exit");
            int choice = scanner.nextInt();

            if (choice == 4) {
                break;
            }

            System.out.print("Enter amount: ");
            double amount = scanner.nextDouble();

            switch (choice) {
                case 1 ->
                    fromAccount.deposit(amount);
                case 2 ->
                    fromAccount.withdraw(amount);
                case 3 -> {
                    System.out.println("Choose an account to transfer to:");
                    for (BankAccount account : allAccounts) {
                        if (account != fromAccount) {
                            System.out.println("Account ID: " + account.getAccountId() + ", Balance: " + account.getBalance());
                        }
                    }
                    System.out.print("Enter Account ID: ");
                    int toAccountId = scanner.nextInt();
                    BankAccount toAccount = allAccounts.stream()
                            .filter(a -> a.getAccountId() == toAccountId)
                            .findFirst()
                            .orElse(null);

                    if (toAccount != null) {
                        fromAccount.transfer(toAccount, amount);
                    } else {
                        System.out.println("Invalid Account ID.");
                    }
                }
                default ->
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

public class BankSimulation {

    private static final Logger logger = Logger.getLogger(BankSimulation.class.getName());

    public static void main(String[] args) throws InterruptedException {
        try {
            FileHandler fileHandler = new FileHandler("transactions.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        List<BankAccount> accounts = new ArrayList<>();
        int accountCounter = 1;

        System.out.println("Welcome to the Banking System Simulation!");

        // Initial accounts
        accounts.add(new BankAccount(accountCounter++, 5000));
        accounts.add(new BankAccount(accountCounter++, 5000));

        while (true) {
            System.out.println("Main Menu:");
            System.out.println("1. Add New Account");
            System.out.println("2. Manage an Existing Account");
            System.out.println("3. Exit");
            int choice = scanner.nextInt();

            if (choice == 3) {
                break;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter initial balance for new account: ");
                    double initialBalance = scanner.nextDouble();
                    BankAccount newAccount = new BankAccount(accountCounter++, initialBalance);
                    accounts.add(newAccount);
                    System.out.println("Account created with ID: " + newAccount.getAccountId());
                }
                case 2 -> {
                    System.out.println("Available Accounts:");
                    for (BankAccount account : accounts) {
                        System.out.println("Account ID: " + account.getAccountId() + ", Balance: " + account.getBalance());
                    }
                    System.out.print("Enter Account ID to manage: ");
                    int accountId = scanner.nextInt();
                    BankAccount selectedAccount = accounts.stream()
                            .filter(a -> a.getAccountId() == accountId)
                            .findFirst()
                            .orElse(null);

                    if (selectedAccount != null) {
                        CustomerThread customerThread = new CustomerThread(selectedAccount, accounts, scanner);
                        customerThread.start();
                        customerThread.join();
                    } else {
                        System.out.println("Invalid Account ID.");
                    }
                }
                default ->
                    System.out.println("Invalid choice. Try again.");
            }
        }

        System.out.println("Final Account Balances:");
        for (BankAccount account : accounts) {
            System.out.println("Account ID: " + account.getAccountId() + ", Balance: " + account.getBalance());
        }
    }
}
