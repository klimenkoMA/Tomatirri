package accountingApp.entity;

public enum TomatoesCategory {

    Гном,
    Индетерминантный,
    Супердетерминантный,
    Детерминантный,
    Полудетерминантный,
    Штамбовый,
    ;

    public String getCategory() {
        return name();
    }

}
