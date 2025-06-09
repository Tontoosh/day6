import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

abstract class Subscription {
    String id, customer, phone, nextDate;
    LocalDate nextDateObj;
    String recurring, plan, status;

    public Subscription(String id, String customer, String phone, LocalDate nextDateObj, String recurring, String plan, String status) {
        this.id = id;
        this.customer = customer;
        this.phone = phone;
        this.nextDateObj = nextDateObj;
        this.nextDate = nextDateObj.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.recurring = recurring;
        this.plan = plan;
        this.status = status;
    }

    public abstract String getType();

    public Object[] toRow() {
        return new Object[]{id, customer, phone, nextDate, recurring, plan, status, getType()};
    }
}

class ProductSubscription extends Subscription {
    public ProductSubscription(String id, String customer, String phone, LocalDate nextDateObj, String recurring, String plan, String status) {
        super(id, customer, phone, nextDateObj, recurring, plan, status);
    }

    @Override
    public String getType() {
        return "Бүтээгдэхүүн";
    }
}

class ServiceSubscription extends Subscription {
    public ServiceSubscription(String id, String customer, String phone, LocalDate nextDateObj, String recurring, String plan, String status) {
        super(id, customer, phone, nextDateObj, recurring, plan, status);
    }

    @Override
    public String getType() {
        return "Үйлчилгээ";
    }
}

class SubscriptionManager {
    private List<Subscription> subscriptions = new ArrayList<>();

    public SubscriptionManager(List<Subscription> initialData) {
        subscriptions.addAll(initialData);
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(String customer, String phone, LocalDate nextDateObj, String recurring, String plan, String status, String type) {
        String id = generateNewId();
        Subscription sub = type.equalsIgnoreCase("Бүтээгдэхүүн") ?
                new ProductSubscription(id, customer, phone, nextDateObj, recurring, plan, status) :
                new ServiceSubscription(id, customer, phone, nextDateObj, recurring, plan, status);
        subscriptions.add(sub);
    }

    public String generateNewId() {
        return "S" + String.format("%04d", subscriptions.size() + 1);
    }

    public Subscription findById(String id) {
        return subscriptions.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
    }

    public void editSubscription(String id, String customer, String phone, LocalDate nextDateObj, String recurring, String plan, String status, String type) {
        Subscription old = findById(id);
        if (old != null) {
            subscriptions.remove(old);
            Subscription updated = type.equalsIgnoreCase("Бүтээгдэхүүн") ?
                    new ProductSubscription(id, customer, phone, nextDateObj, recurring, plan, status) :
                    new ServiceSubscription(id, customer, phone, nextDateObj, recurring, plan, status);
            subscriptions.add(updated);
        }
    }

    public void removeSubscription(String id) {
        subscriptions.removeIf(s -> s.id.equals(id));
    }

    public List<Subscription> filter(String keyword) {
        keyword = keyword.toLowerCase();
        List<Subscription> result = new ArrayList<>();
        for (Subscription s : subscriptions) {
            if (s.id.toLowerCase().contains(keyword) ||
                    s.customer.toLowerCase().contains(keyword) ||
                    s.phone.contains(keyword) ||
                    s.plan.toLowerCase().contains(keyword) ||
                    s.status.toLowerCase().contains(keyword)) {
                result.add(s);
            }
        }
        return result;
    }
}

public class SubscriptionManagerGUI extends JFrame {
    private SubscriptionManager manager;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public SubscriptionManagerGUI() {
        List<Subscription> initialSubs = List.of(
                new ProductSubscription("S0001", "Бат Энх", "99119911", LocalDate.parse("09-25-2025", dateFormatter), "$35.00", "Сар бүр", "Идэвхтэй"),
                new ServiceSubscription("S0002", "Эрхэс Бат", "99112233", LocalDate.parse("09-25-2025", dateFormatter), "$35.00", "Сар бүр", "Идэвхтэй")
        );
        manager = new SubscriptionManager(initialSubs);

        setTitle("Захиалгын Менежер");
        setSize(900, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Дугаар", "Хэрэглэгч", "Утас", "Дараагийн огноо", "Дахин төлбөр", "Төлөвлөгөө", "Төлөв", "Төрөл"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Нэмэх");
        JButton editBtn = new JButton("Засах");
        JButton removeBtn = new JButton("Устгах");
        searchField = new JTextField(15);

        topPanel.add(addBtn);
        topPanel.add(editBtn);
        topPanel.add(removeBtn);
        topPanel.add(new JLabel("Хайх:"));
        topPanel.add(searchField);
        add(topPanel, BorderLayout.NORTH);

        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                Subscription sub = manager.findById(id);
                showForm(sub);
            }
        });
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                manager.removeSubscription(id);
                refreshTable();
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });

        refreshTable();
    }

    private void showForm(Subscription existing) {
        JTextField customer = new JTextField(existing != null ? existing.customer : "");
        JTextField phone = new JTextField(existing != null ? existing.phone : "");
        JTextField nextDate = new JTextField(existing != null ? existing.nextDate : "");
        JTextField recurring = new JTextField(existing != null ? existing.recurring : "");
        JComboBox<String> planBox = new JComboBox<>(new String[]{"Сар бүр", "Жил бүр"});
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Идэвхтэй", "Төлөвлөсөн", "Хаагдсан"});
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Бүтээгдэхүүн", "Үйлчилгээ"});

        if (existing != null) {
            planBox.setSelectedItem(existing.plan);
            statusBox.setSelectedItem(existing.status);
            typeBox.setSelectedItem(existing.getType());
        }

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Хэрэглэгчийн нэр:")); panel.add(customer);
        panel.add(new JLabel("Утас (8 оронтой):")); panel.add(phone);
        panel.add(new JLabel("Дараагийн огноо (MM-dd-yyyy):")); panel.add(nextDate);
        panel.add(new JLabel("Дахин төлбөр (жишээ: $35.00):")); panel.add(recurring);
        panel.add(new JLabel("Төлөвлөгөө:")); panel.add(planBox);
        panel.add(new JLabel("Төлөв:")); panel.add(statusBox);
        panel.add(new JLabel("Төрөл:")); panel.add(typeBox);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, existing == null ? "Захиалга нэмэх" : "Захиалга засах", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                break;
            }
            try {
                String custVal = customer.getText().trim();
                String phoneVal = phone.getText().trim();
                String nextDateVal = nextDate.getText().trim();
                String recurringVal = recurring.getText().trim();

                if (custVal.isEmpty() || custVal.length() < 2) {
                    throw new IllegalArgumentException("Хэрэглэгчийн нэр дутуу байна.");
                }
                if (!phoneVal.matches("\\d{8}")) {
                    throw new IllegalArgumentException("Утасны дугаарыг зөв оруулна уу (8 оронтой).");
                }
                if (!recurringVal.matches("\\d+(\\.\\d{2})?")) {
                    throw new IllegalArgumentException("Дахин төлбөрийг зөв оруулна уу (жишээ: 35.00).");
                }
                double amount = Double.parseDouble(recurringVal.replace("$", ""));
                if (amount < 0.01) {
                    throw new IllegalArgumentException("Дахин төлбөрийн дүн $0.01-аас бага байж болохгүй.");
                }

                LocalDate nextDateObj = LocalDate.parse(nextDateVal, dateFormatter);

                if (existing == null) {
                    manager.addSubscription(custVal, phoneVal, nextDateObj, recurringVal,
                            planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
                } else {
                    manager.editSubscription(existing.id, custVal, phoneVal, nextDateObj, recurringVal,
                            planBox.getSelectedItem().toString(), statusBox.getSelectedItem().toString(), typeBox.getSelectedItem().toString());
                }
                refreshTable();
                break;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Алдаа", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Дараагийн огноог зөв оруулна уу (жишээ: 09-25-2025).", "Алдаа", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Алдаа гарлаа: " + ex.getMessage(), "Алдаа", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText();
        List<Subscription> list = keyword.isEmpty() ? manager.getSubscriptions() : manager.filter(keyword);
        for (Subscription s : list) {
            tableModel.addRow(s.toRow());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SubscriptionManagerGUI().setVisible(true));
    }
}
