package by.popolamov.deoreader.presenters;

import by.popolamov.deoreader.views.MainView;

public class HomePresenter {
    private final MainView view;

    public HomePresenter(MainView view) {
        this.view = view;
    }

    public void expandLeftPane() {
        view.expandLeftPane();
    }
}
