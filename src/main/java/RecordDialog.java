import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Диалоговое окно для добавления или редактирования записи в таблице.
 */
public class RecordDialog extends JDialog {

    /**
     * Поле для ввода ФИО водителя.
     */
    private JTextField nameField;
    /**
     * Поле для ввода номера машины.
     */
    private JTextField licenseField;
    /**
     * Поле для ввода даты нарушения.
     */
    private JTextField dateField;
    /**
     * Поле для ввода типа нарушения.
     */
    private JTextField violationField;
    /**
     * Модель таблицы, используемая для добавления или обновления записи.
     */
    private DefaultTableModel tableModel;
    /**
     * Индекс строки для редактирования, -1 если добавляется новая запись.
     */
    private int rowIndex = -1;
    private static final Logger logger = LogManager.getLogger(RecordDialog.class);


    /**
     * Конструктор для создания окна записи (добавление или редактирование).
     *
     * @param parentFrame Родительское окно
     * @param tableModel  Модель таблицы
     * @param rowIndex    Индекс строки для редактирования (-1 для добавления новой записи)
     */
    public RecordDialog(JFrame parentFrame, DefaultTableModel tableModel, int rowIndex) {
        super(parentFrame, rowIndex == -1 ? "Добавить запись" : "Редактировать запись", true);
        this.tableModel = tableModel;
        this.rowIndex = rowIndex;

        setSize(350, 250);
        setLocationRelativeTo(parentFrame);
        setLayout(new BorderLayout(10, 10));

        // Панель с полями ввода
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        nameField = new JTextField(15);
        licenseField = new JTextField(10);
        dateField = new JTextField(10);
        violationField = new JTextField(15);

        inputPanel.add(new JLabel("ФИО водителя:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Номер машины:"));
        inputPanel.add(licenseField);
        inputPanel.add(new JLabel("Дата нарушения (ДД.ММ.ГГГГ):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Тип нарушения:"));
        inputPanel.add(violationField);

        // Заполнение полей, если это редактирование
        if (rowIndex != -1) {
            nameField.setText((String) tableModel.getValueAt(rowIndex, 0));
            licenseField.setText((String) tableModel.getValueAt(rowIndex, 1));
            dateField.setText((String) tableModel.getValueAt(rowIndex, 2));
            violationField.setText((String) tableModel.getValueAt(rowIndex, 3));
        }

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton(rowIndex == -1 ? "Сохранить" : "Обновить");
        JButton cancelButton = new JButton("Отмена");

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndSave()) {
                    dispose();
                }
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Добавление панелей в диалоговое окно
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Проверяет корректность данных и сохраняет их, если данные верны.
     *
     * @return true, если данные успешно сохранены, иначе false
     */
    private boolean validateAndSave() {
        logger.info("Начата проверка данных формы редактирования.");
        if (nameField.getText().isEmpty() || licenseField.getText().isEmpty() ||
                dateField.getText().isEmpty() || violationField.getText().isEmpty()) {
            logger.warn("Проверка не пройдена: не все поля заполнены.");
            JOptionPane.showMessageDialog(this, "Все поля должны быть заполнены.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        logger.debug("Все поля заполнены.");

        // Проверка формата российского номера
        if (!licenseField.getText().matches("^[АВЕКМНОРСТУХ]{1}\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$")) {
            logger.warn("Проверка не пройдена: неверный формат номера '{}'.", licenseField.getText());
            JOptionPane.showMessageDialog(this, "Неверный формат номера. Введите российский номер (например, А123ВС77).", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        logger.debug("Формат номера '{}' прошёл проверку.", licenseField.getText());

        // Проверка даты нарушения
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate violationDate;
        try {
            violationDate = LocalDate.parse(dateField.getText(), formatter);
        } catch (DateTimeParseException ex) {
            logger.error("Неверный формат даты '{}'.", dateField.getText(), ex);
            JOptionPane.showMessageDialog(this, "Неверный формат даты. Используйте ДД.ММ.ГГГГ.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        logger.debug("Дата '{}' успешно распознана.", dateField.getText());
        LocalDate currentDate = LocalDate.now();
        LocalDate fiveYearsAgo = currentDate.minusYears(5);

        // Проверка: дата не должна быть в будущем и не старше 5 лет
        if (violationDate.isAfter(currentDate)) {
            logger.warn("Дата нарушения '{}' больше текущей даты.", dateField.getText());
            JOptionPane.showMessageDialog(this, "Дата нарушения не может быть в будущем.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (violationDate.isBefore(fiveYearsAgo)) {
            logger.warn("Дата нарушения '{}' старше 5 лет.", dateField.getText());
            JOptionPane.showMessageDialog(this, "Нарушение не должно быть старше 5 лет.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        logger.debug("Дата '{}' прошла проверку.", dateField.getText());

        // Сохранение данных
        if (rowIndex == -1) {
            logger.info("Добавление новой записи: {}, {}, {}, {}.",
                    nameField.getText(), licenseField.getText(), dateField.getText(), violationField.getText());
            tableModel.addRow(new Object[]{nameField.getText(), licenseField.getText(), dateField.getText(), violationField.getText()});
        } else {
            logger.info("Обновление записи в строке {}: {}, {}, {}, {}.",
                    rowIndex, nameField.getText(), licenseField.getText(), dateField.getText(), violationField.getText());
            tableModel.setValueAt(nameField.getText(), rowIndex, 0);
            tableModel.setValueAt(licenseField.getText(), rowIndex, 1);
            tableModel.setValueAt(dateField.getText(), rowIndex, 2);
            tableModel.setValueAt(violationField.getText(), rowIndex, 3);
        }
        logger.info("Сохранение данных успешно завершено.");
        return true;
    }
}
