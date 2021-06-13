import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
class ShowTablename extends JFrame implements ActionListener {

    JFrame frame;
    JPanel panel;
    JLabel label;
    JButton selectButton;
    JTextField jTextField;

    public ShowTablename(String[] tablename) {
        frame = new JFrame("表");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        frame.setLocationRelativeTo(null);
        frame.setSize(350,200);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());//设置为流式布局
        label = new JLabel("表名");
        selectButton = new JButton("选择");
        selectButton.addActionListener(this);//监听事件
        jTextField = new JTextField(23);//设置文本框的长度
        JList jList = new JList(tablename);


        panel.add(label);//把组件添加到面板panel
        panel.add(jList);
        panel.add(jTextField);
        panel.add(selectButton);

        frame.add(panel);//实现面板panel

        frame.setVisible(true);//设置可见
    }

    @SuppressWarnings("deprecation")
    @Override
    public void actionPerformed(ActionEvent e) {//处理事件
        // TODO Auto-generated method stub
        if (e.getSource()==selectButton) {
            String table=jTextField.getText();
            frame.dispose();
            Llogin.structure(table);
        }
    }
}

