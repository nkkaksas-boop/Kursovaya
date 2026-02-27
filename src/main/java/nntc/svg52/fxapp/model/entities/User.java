package nntc.svg52.fxapp.model.entities;

public class User {
    private int id;
    private String username;
    private String password;
    private int roleId;
    private String roleName;
    private String fullName;

    // Конструкторы
    public User() {}

    public User(int id, String username, String roleName, String fullName) {
        this.id = id;
        this.username = username;
        this.roleName = roleName;
        this.fullName = fullName;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isManager() { return "manager".equals(roleName); }
    public boolean isWaiter() { return "waiter".equals(roleName); }
    public boolean isClient() { return "client".equals(roleName); }
}