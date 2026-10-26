package electricbillingmanagementsystem;


import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

abstract class Customer implements Serializable {
    private String name, username, password, billNumber, lastPaymentDate;
    private long initialRead;
    private double lastBillAmount, lastConsumption;
    private boolean isPaid;

    public Customer(String name, String username, String password, String billNumber, long initialRead) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.billNumber = billNumber;
        this.initialRead = initialRead;
        this.isPaid = false;
        this.lastPaymentDate = "N/A";
    }

    public abstract double calculate(long initialRead, long finalRead);

    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getBillNumber() { return billNumber; }
    public long getInitialRead() { return initialRead; }
    public boolean isPaid() { return isPaid; }
    public String getLastPaymentDate() { return lastPaymentDate; }

    public void updateConsumption(long finalRead) {
        if (finalRead < initialRead) throw new IllegalArgumentException("Final read cannot be less than initial read.");
        long consumptionWatts = finalRead - initialRead;
        this.lastConsumption = consumptionWatts / 1000.0;
        this.lastBillAmount = this.calculate(initialRead, finalRead);
        this.isPaid = false;
    }

    public double getLastConsumption() { return lastConsumption; }
    public double getLastBillAmount() { return lastBillAmount; }

    public void makePayment(String paymentDate) {
        if (lastBillAmount == 0.0) throw new IllegalStateException("No bill to pay.");
        if (isPaid) throw new IllegalStateException("Bill already paid.");
        this.isPaid = true;
        this.lastPaymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Customer{name='" + name + "', username='" + username + "', billNumber='" + billNumber + "'}";
    }
}

class ResidentialCustomer extends Customer {
    private static double[] rates = {0.42, 1.13, 2.15, 2.92, 3.12, 3.39, 3.35};
    private static double[] fees = {10.47, 43.90, 43.90, 43.90, 43.90, 43.90, 43.90};

    public ResidentialCustomer(String name, String username, String password, String billNumber, long initialRead) {
        super(name, username, password, billNumber, initialRead);
    }

    public static double getRate(int tier) {
        if (tier < 0 || tier >= rates.length) throw new IllegalArgumentException("Invalid tier");
        return rates[tier];
    }

    public static double getFee(int tier) {
        if (tier < 0 || tier >= fees.length) throw new IllegalArgumentException("Invalid tier");
        return fees[tier];
    }

    public static void updateRate(int tier, double newRate) {
        if (tier < 0 || tier >= rates.length || newRate < 0) throw new IllegalArgumentException("Invalid tier or rate");
        rates[tier] = newRate;
    }

    public static void updateFee(int tier, double newFee) {
        if (tier < 0 || tier >= fees.length || newFee < 0) throw new IllegalArgumentException("Invalid tier or fee");
        fees[tier] = newFee;
    }

    public static void setRates(double[] newRates) {
        if (newRates == null || newRates.length != 7) throw new IllegalArgumentException("Rates array must have 7 elements");
        for (double r : newRates) if (r < 0) throw new IllegalArgumentException("Rates cannot be negative");
        rates = newRates.clone();
    }

    public static void setFees(double[] newFees) {
        if (newFees == null || newFees.length != 7) throw new IllegalArgumentException("Fees array must have 7 elements");
        for (double f : newFees) if (f < 0) throw new IllegalArgumentException("Fees cannot be negative");
        fees = newFees.clone();
    }

    public double getInstanceRate(int tier) { return getRate(tier); }
    public double getInstanceFee(int tier) { return getFee(tier); }

    @Override
    public double calculate(long initialRead, long finalRead) {
        long consumptionWatts = finalRead - initialRead;
        double consumptionKilowatts = Math.max(0, consumptionWatts / 1000.0);
        double rate, fee;
        if (consumptionKilowatts >= 1 && consumptionKilowatts <= 50) { rate = rates[0]; fee = fees[0]; }
        else if (consumptionKilowatts <= 100) { rate = rates[1]; fee = fees[1]; }
        else if (consumptionKilowatts <= 200) { rate = rates[2]; fee = fees[2]; }
        else if (consumptionKilowatts <= 300) { rate = rates[3]; fee = fees[3]; }
        else if (consumptionKilowatts <= 400) { rate = rates[4]; fee = fees[4]; }
        else if (consumptionKilowatts <= 500) { rate = rates[5]; fee = fees[5]; }
        else if (consumptionKilowatts > 500) { rate = rates[6]; fee = fees[6]; }
        else { rate = 0.0; fee = 0.0; }
        return (consumptionKilowatts * rate) + fee;
    }
}

class CommercialCustomer extends Customer {
    private static double rate = 2.12;
    private static double additionalFee = 56.38;

    public CommercialCustomer(String name, String username, String password, String billNumber, long initialRead) {
        super(name, username, password, billNumber, initialRead);
    }

    public static double getRate() { return rate; }
    public static double getAdditionalFee() { return additionalFee; }

    public static void setRate(double newRate) {
        if (newRate < 0) throw new IllegalArgumentException("Rate cannot be negative");
        rate = newRate;
    }

    public static void setAdditionalFee(double newFee) {
        if (newFee < 0) throw new IllegalArgumentException("Fee cannot be negative");
        additionalFee = newFee;
    }

    @Override
    public double calculate(long initialRead, long finalRead) {
        long consumptionWatts = finalRead - initialRead;
        double consumptionKilowatts = Math.max(0, consumptionWatts / 1000.0);
        return (consumptionKilowatts * rate) + additionalFee;
    }
}

class ElectricityBillingSystem {
    private List<Customer> customers = new ArrayList<>();

    public ElectricityBillingSystem() { seedCustomers(); }

    private void seedCustomers() {
        customers.add(new ResidentialCustomer("Abebe Kebede", "ABE001", "AG1101", "21234543", 4350));
        customers.add(new CommercialCustomer("Kassahun Mengesha", "KAS010", "KR5475", "6547839", 234155));
        customers.add(new ResidentialCustomer("Yalemtsehay Alemu", "YAL101", "YG7172", "435217534", 23450));
        customers.add(new CommercialCustomer("Tsehaynesh Solomon", "TSE721", "TN0174", "12309777", 679872));
        customers.add(new ResidentialCustomer("Fikadu Desta", "FKD981", "FA1111", "4352423", 54545465));
    }

    public Customer validateUser(String username, String password) {
        for (Customer c : customers) if (c.getUsername().equals(username) && c.getPassword().equals(password)) return c;
        return null;
    }

    public Customer findCustomerByBillNumber(String billNumber) {
        for (Customer c : customers) if (c.getBillNumber().equals(billNumber)) return c;
        return null;
    }

    public void addCustomer(Customer customer) { customers.add(customer); saveSystem(); }

    public int getPaidCustomersCount() {
        int count = 0;
        for (Customer c : customers) if (c.isPaid()) count++;
        return count;
    }

    public int getUnpaidCustomersCount() { return customers.size() - getPaidCustomersCount(); }

    public void saveSystem() {
        try {
            java.io.FileOutputStream fos = new java.io.FileOutputStream("billingSystem.dat");
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
            oos.writeObject(customers);
            oos.close();
            fos.close();
            System.out.println("Data saved successfully.");
        } catch (Exception e) { System.out.println("Error saving: " + e.getMessage()); }
    }

    public void loadSystem() {
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream("billingSystem.dat");
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
            customers = (List<Customer>) ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            if (!e.toString().contains("FileNotFoundException")) {
                System.out.println("Error loading: " + e.getMessage());
            }
            // Removed the else block to erase the "No saved data found, using defaults." message
        }
    }
}

class AdminUser {
    private String username, password;

    public AdminUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean validate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void displayPaymentStats(ElectricityBillingSystem system) {
        int paid = system.getPaidCustomersCount(), unpaid = system.getUnpaidCustomersCount();
        System.out.println("Payment Statistics: Paid: " + paid + ", Unpaid: " + unpaid);
    }

    public void updateResidentialRates(double[] newRates, double[] newFees) {
        try {
            ResidentialCustomer.setRates(newRates);
            ResidentialCustomer.setFees(newFees);
            System.out.println("Residential rates/fees updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void updateResidentialRate(int tier, double newRate) {
        try {
            ResidentialCustomer.updateRate(tier, newRate);
            System.out.println("Rate tier " + tier + " updated to " + newRate);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void updateResidentialFee(int tier, double newFee) {
        try {
            ResidentialCustomer.updateFee(tier, newFee);
            System.out.println("Fee tier " + tier + " updated to " + newFee);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public double getResidentialRate(int tier) { return ResidentialCustomer.getRate(tier); }
    public double getResidentialFee(int tier) { return ResidentialCustomer.getFee(tier); }

    public void updateCommercialRates(double newRate, double newFee) {
        try {
            CommercialCustomer.setRate(newRate);
            CommercialCustomer.setAdditionalFee(newFee);
            System.out.println("Commercial rate/fee updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

class MeterUser {
    private String username, password;

    public MeterUser(String username, String password) { this.username = username; this.password = password; }

    public boolean validate(String username, String password) { return this.username.equals(username) && this.password.equals(password); }

    public boolean canCollect() { int day = LocalDate.now().getDayOfMonth(); return day >= 15 && day <= 24; }

    public void recordConsumption(Customer customer, long finalRead) {
        if (!canCollect()) throw new IllegalStateException("Meter readings only between 15th and 24th!");
        customer.updateConsumption(finalRead);
    }
}

class RegistrarUser {
    private String username, password;

    public RegistrarUser(String username, String password) { this.username = username; this.password = password; }

    public boolean validate(String username, String password) { return this.username.equals(username) && this.password.equals(password); }

    public void addCustomer(ElectricityBillingSystem system, Customer customer) { system.addCustomer(customer); System.out.println("Customer registered: " + customer); }
}

class PaymentReceipt {
    private String receiptNumber;
    private Customer customer;
    private double amountPaid;
    private String paymentDate;
    private String paymentMethod;

    public PaymentReceipt(Customer customer, String paymentMethod) {
        this.customer = customer;
        this.amountPaid = customer.getLastBillAmount();
        this.paymentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.receiptNumber = generateReceiptNumber();
    }

    private String generateReceiptNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RCP-" + customer.getBillNumber() + "-" + timestamp;
    }

    public String generateReceiptText() {
        return "═══════════════════════════════════════════════════\n" +
               "         ETHIOPIAN ELECTRICITY UTILITY\n" +
               "              PAYMENT RECEIPT\n" +
               "═══════════════════════════════════════════════════\n\n" +
               "Receipt Number: " + receiptNumber + "\n" +
               "Payment Date:   " + paymentDate + "\n\n" +
               "---------------------------------------------------\n" +
               "CUSTOMER INFORMATION\n" +
               "---------------------------------------------------\n" +
               "Name:           " + customer.getName() + "\n" +
               "Bill Number:    " + customer.getBillNumber() + "\n" +
               "Username:       " + customer.getUsername() + "\n\n" +
               "---------------------------------------------------\n" +
               "BILLING DETAILS\n" +
               "---------------------------------------------------\n" +
               "Consumption:    " + String.format("%.2f", customer.getLastConsumption()) + " kWh\n" +
               "Amount Paid:    " + String.format("%.2f", amountPaid) + " Birr\n" +
               "Payment Method: " + paymentMethod + "\n\n" +
               "---------------------------------------------------\n" +
               "Status:         PAID ✓\n" +
               "---------------------------------------------------\n\n" +
               "Thank you! Call 7483 or visit www.eepco.gov.et\n" +
               "═══════════════════════════════════════════════════\n";
    }
}
public class ElectricBillingManagementSystem {
    private ElectricityBillingSystem system;
    private MeterUser meterUser;
    private AdminUser adminUser;
    private RegistrarUser registrarUser;
    private JFrame frame;
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton logoutButton;  // Added logout button for login page

    public ElectricBillingManagementSystem() {
        this.system = new ElectricityBillingSystem();
        this.meterUser = new MeterUser("METER01", "MTR123");
        this.adminUser = new AdminUser("ADMIN01", "ADM123");
        this.registrarUser = new RegistrarUser("REG01", "REG123");
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Ethiopian Electricity Billing System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);  // Reduced size by half
        frame.setLocationRelativeTo(null);

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);  // Reduced insets for smaller size

        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);  // Reduced width
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);  // Reduced width
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        loginButton = new JButton("Login");
        loginButton.setBackground(Color.GREEN);  // Green login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);  // Red logout button
        logoutButton.addActionListener(e -> {
            // On login page, logout clears the fields
            usernameField.setText("");
            passwordField.setText("");
        });
        loginPanel.add(logoutButton, gbc);

        frame.add(loginPanel);
        frame.setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter both username and password.");
            return;
        }
        Customer customer = system.validateUser(username, password);
        if (customer != null) {
            showCustomerDashboard(customer);
            return;
        }
        if (meterUser.validate(username, password)) {
            showMeterDashboard();
            return;
        }
        if (adminUser.validate(username, password)) {
            showAdminDashboard();
            return;
        }
        if (registrarUser.validate(username, password)) {
            showRegistrarDashboard();
            return;
        }
        JOptionPane.showMessageDialog(frame, "Invalid credentials.");
    }

    private void showCustomerDashboard(Customer customer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Welcome, " + customer.getName() + "! Bill Number: " + customer.getBillNumber()), BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        if (customer.getLastConsumption() == 0.0) {
            infoPanel.add(new JLabel("No consumption recorded."));
        } else {
            infoPanel.add(new JLabel("Consumption: " + String.format("%.2f", customer.getLastConsumption()) + " kWh"));
            infoPanel.add(new JLabel("Bill: " + String.format("%.2f", customer.getLastBillAmount()) + " Birr"));
            infoPanel.add(new JLabel("Status: " + (customer.isPaid() ? "PAID" : "UNPAID")));
        }
        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        if (customer.getLastConsumption() != 0.0) {
            if (customer.isPaid()) {
                JButton viewReceiptButton = new JButton("View Receipt");
                viewReceiptButton.addActionListener(e -> displayReceipt(customer, "CBE Birr"));
                buttonPanel.add(viewReceiptButton);
            } else {
                JButton payBillButton = new JButton("Pay Bill");
                payBillButton.addActionListener(e -> handlePayment(customer));
                buttonPanel.add(payBillButton);
            }
        }
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(backButton);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);  // Red logout button
        logoutButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void handlePayment(Customer customer) {
        int result = JOptionPane.showConfirmDialog(frame, "Proceed with payment of " + String.format("%.2f", customer.getLastBillAmount()) + " Birr?", "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                customer.makePayment(date);
                system.saveSystem();
                JOptionPane.showMessageDialog(frame, "Payment successful!\nAmount: " + String.format("%.2f", customer.getLastBillAmount()) + " Birr\nDate: " + date);
                int receiptResult = JOptionPane.showConfirmDialog(frame, "View Receipt?", "Receipt", JOptionPane.YES_NO_OPTION);
                if (receiptResult == JOptionPane.YES_OPTION) {
                    displayReceipt(customer, "CBE Birr");
                }
                showCustomerDashboard(customer); // Refresh dashboard
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        }
    }

    private void displayReceipt(Customer customer, String method) {
        PaymentReceipt receipt = new PaymentReceipt(customer, method);
        JTextArea textArea = new JTextArea(receipt.generateReceiptText());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(250, 200));  // Reduced size
        JOptionPane.showMessageDialog(frame, scrollPane, "Payment Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMeterDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Meter Dashboard"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton recordReadingButton = new JButton("Record Reading");
        recordReadingButton.addActionListener(e -> recordMeterReading());
        buttonPanel.add(recordReadingButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(backButton);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);  // Red logout button
        logoutButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void recordMeterReading() {
        if (!meterUser.canCollect()) {
            JOptionPane.showMessageDialog(frame, "Error: Meter readings can only be collected between 15th and 24th!");
            return;
        }
        String billNumber = JOptionPane.showInputDialog(frame, "Enter Customer Bill Number:");
        if (billNumber == null || billNumber.trim().isEmpty()) return;
        Customer customer = system.findCustomerByBillNumber(billNumber.trim());
        if (customer == null) {
            JOptionPane.showMessageDialog(frame, "Error: Customer not found!");
            return;
        }
        String consumptionStr = JOptionPane.showInputDialog(frame, "Enter Monthly Consumption (watts):");
        if (consumptionStr == null || consumptionStr.trim().isEmpty()) return;
        try {
            long consumption = Long.parseLong(consumptionStr.trim());
            if (consumption < 0) {
                JOptionPane.showMessageDialog(frame, "Error: Consumption cannot be negative!");
                return;
            }
            long finalRead = customer.getInitialRead() + consumption;
            meterUser.recordConsumption(customer, finalRead);
            system.saveSystem();
            JOptionPane.showMessageDialog(frame, "Consumption recorded successfully!\nCustomer: " + customer.getName() + "\nBill Number: " + customer.getBillNumber());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Error: Invalid consumption value!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void showAdminDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Admin Dashboard"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        JButton viewStatsButton = new JButton("View Payment Statistics");
        viewStatsButton.addActionListener(e -> {
            int paid = system.getPaidCustomersCount();
            int unpaid = system.getUnpaidCustomersCount();
            JOptionPane.showMessageDialog(frame, "Payment Statistics:\nPaid: " + paid + "\nUnpaid: " + unpaid);
        });
        buttonPanel.add(viewStatsButton);

        JButton updateResidentialButton = new JButton("Update Residential Rates");
        updateResidentialButton.addActionListener(e -> updateResidentialRatesUI());
        buttonPanel.add(updateResidentialButton);

        JButton updateCommercialButton = new JButton("Update Commercial Rates");
        updateCommercialButton.addActionListener(e -> updateCommercialRatesUI());
        buttonPanel.add(updateCommercialButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(backButton);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);  // Red logout button
        logoutButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void updateResidentialRatesUI() {
        String[] options = {"Update Rates", "Update Fees", "Update Both"};
        int choice = JOptionPane.showOptionDialog(frame, "What would you like to update?", "Residential Rate Update", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == JOptionPane.CLOSED_OPTION) return;
        boolean updateRates = (choice == 0 || choice == 2);
        boolean updateFees = (choice == 1 || choice == 2);
        double[] currentRates = {0.42, 1.13, 2.15, 2.92, 3.12, 3.39, 3.35};
        double[] currentFees = {10.47, 43.90, 43.90, 43.90, 43.90, 43.90, 43.90};
        double[] newRates = currentRates.clone();
        double[] newFees = currentFees.clone();
        if (updateRates) {
            String[] tierOptions = {"Tier 1 (1-50 kWh)", "Tier 2 (51-100 kWh)", "Tier 3 (101-200 kWh)", "Tier 4 (201-300 kWh)", "Tier 5 (301-400 kWh)", "Tier 6 (401-500 kWh)", "Tier 7 (>500 kWh)"};
            JComboBox<String> tierCombo = new JComboBox<>(tierOptions);
            int tierChoice = JOptionPane.showConfirmDialog(frame, tierCombo, "Select Tier for Rate Update", JOptionPane.OK_CANCEL_OPTION);
            if (tierChoice == JOptionPane.OK_OPTION) {
                int tier = tierCombo.getSelectedIndex();
                String rateStr = JOptionPane.showInputDialog(frame, "Current Rate: " + currentRates[tier] + "\nEnter new rate:");
                if (rateStr == null) return;
                try {
                    double newRate = Double.parseDouble(rateStr.trim());
                    if (newRate < 0) {
                        JOptionPane.showMessageDialog(frame, "Rate cannot be negative!");
                        return;
                    }
                    newRates[tier] = newRate;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid input!");
                    return;
                }
            }
        }
        if (updateFees) {
            String[] tierOptions = {"Tier 1 (1-50 kWh)", "Tier 2 (51-100 kWh)", "Tier 3 (101-200 kWh)", "Tier 4 (201-300 kWh)", "Tier 5 (301-400 kWh)", "Tier 6 (401-500 kWh)", "Tier 7 (>500 kWh)"};
            JComboBox<String> tierCombo = new JComboBox<>(tierOptions);
            int tierChoice = JOptionPane.showConfirmDialog(frame, tierCombo, "Select Tier for Fee Update", JOptionPane.OK_CANCEL_OPTION);
            if (tierChoice == JOptionPane.OK_OPTION) {
                int tier = tierCombo.getSelectedIndex();
                String feeStr = JOptionPane.showInputDialog(frame, "Current Fee: " + currentFees[tier] + "\nEnter new fee:");
                if (feeStr == null) return;
                try {
                    double newFee = Double.parseDouble(feeStr.trim());
                    if (newFee < 0) {
                        JOptionPane.showMessageDialog(frame, "Fee cannot be negative!");
                        return;
                    }
                    newFees[tier] = newFee;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Invalid input!");
                    return;
                }
            }
        }
        try {
            adminUser.updateResidentialRates(newRates, newFees);
            system.saveSystem();
            JOptionPane.showMessageDialog(frame, "Residential rates/fees updated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void updateCommercialRatesUI() {
        String[] options = {"Update Rate", "Update Fee", "Update Both"};
        int choice = JOptionPane.showOptionDialog(frame, "What would you like to update?", "Commercial Rate Update", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == JOptionPane.CLOSED_OPTION) return;
        boolean updateRate = (choice == 0 || choice == 2);
        boolean updateFee = (choice == 1 || choice == 2);
        double currentRate = 2.12;
        double currentFee = 56.38;
        double newRate = currentRate;
        double newFee = currentFee;
        if (updateRate) {
            String rateStr = JOptionPane.showInputDialog(frame, "Current Rate: " + currentRate + "\nEnter new rate:");
            if (rateStr == null) return;
            try {
                newRate = Double.parseDouble(rateStr.trim());
                if (newRate < 0) {
                    JOptionPane.showMessageDialog(frame, "Rate cannot be negative!");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input!");
                return;
            }
        }
        if (updateFee) {
            String feeStr = JOptionPane.showInputDialog(frame, "Current Fee: " + currentFee + "\nEnter new fee:");
            if (feeStr == null) return;
            try {
                newFee = Double.parseDouble(feeStr.trim());
                if (newFee < 0) {
                    JOptionPane.showMessageDialog(frame, "Fee cannot be negative!");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input!");
                return;
            }
        }
        try {
            adminUser.updateCommercialRates(newRate, newFee);
            system.saveSystem();
            JOptionPane.showMessageDialog(frame, "Commercial rates/fees updated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void showRegistrarDashboard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Registrar Dashboard"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton registerCustomerButton = new JButton("Register New Customer");
        registerCustomerButton.addActionListener(e -> registerNewCustomer());
        buttonPanel.add(registerCustomerButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(backButton);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.RED);  // Red logout button
        logoutButton.addActionListener(e -> {
            frame.setContentPane(loginPanel);
            frame.revalidate();
            frame.repaint();
        });
        buttonPanel.add(logoutButton);
        panel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void registerNewCustomer() {
        JTextField nameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField billNumberField = new JTextField();
        JTextField initialReadField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Residential", "Commercial"});

        Object[] message = {
            "Name:", nameField,
            "Username:", usernameField,
            "Password:", passwordField,
            "Bill Number:", billNumberField,
            "Initial Reading (watts):", initialReadField,
            "Customer Type:", typeCombo
        };

        int option = JOptionPane.showConfirmDialog(frame,        message, "Register New Customer", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String billNumber = billNumberField.getText().trim();
            String initialReadStr = initialReadField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || billNumber.isEmpty() || initialReadStr.isEmpty() || type == null) {
                JOptionPane.showMessageDialog(frame, "All fields are required!");
                return;
            }
            try {
                long initialRead = Long.parseLong(initialReadStr);
                Customer newCustomer = type.equals("Residential") ? new ResidentialCustomer(name, username, password, billNumber, initialRead) : new CommercialCustomer(name, username, password, billNumber, initialRead);
                registrarUser.addCustomer(system, newCustomer);
                JOptionPane.showMessageDialog(frame, "Customer registered successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid initial reading!");
            }
        }
    }

    public static void main(String[] args) {
        ElectricBillingManagementSystem app = new ElectricBillingManagementSystem();
        app.system.loadSystem();
    }
}