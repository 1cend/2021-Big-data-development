import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
class login extends JFrame implements ActionListener {

    JFrame frame;
    JPanel panel;
    JLabel label,label2;
    JButton loginButton,exitButton;
    JTextField jTextField;
    JPasswordField passwordField;

    public login() {
        frame = new JFrame("用户登录界面");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        frame.setLocationRelativeTo(null);
        frame.setSize(350,200);
        panel = new JPanel();
        panel.setLayout(new FlowLayout());//设置为流式布局
        label = new JLabel("用户名");
        label2 = new JLabel("密    码");
        loginButton = new JButton("登录");
        loginButton.addActionListener(this);//监听事件
        exitButton = new JButton("退出");
        exitButton.addActionListener(this);//监听事件
        jTextField = new JTextField(23);//设置文本框的长度
        passwordField = new JPasswordField(23);//设置密码框

        panel.add(label);//把组件添加到面板panel
        panel.add(jTextField);
        panel.add(label2);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(exitButton);

        frame.add(panel);//实现面板panel

        frame.setVisible(true);//设置可见
    }

    @SuppressWarnings("deprecation")
    @Override
    public void actionPerformed(ActionEvent e) {//处理事件
        // TODO Auto-generated method stub
        if (e.getSource()==loginButton) {
            String username=jTextField.getText();
            String password=passwordField.getText();
            Llogin.llogin(username,password);
            JOptionPane.showMessageDialog(null,"登录成功！" );
            frame.dispose();
            //new ShowTable();
            }
            if (e.getSource()==exitButton) {
                System.exit(0);
            }
        }

    public static void main(String[] args) {
        new login();//调用login方法
    }
}

