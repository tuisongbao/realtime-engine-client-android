package com.tuisongbao.engine.demo.widght.swipe.interfaces;

import com.tuisongbao.engine.demo.widght.swipe.SwipeLayout;
import com.tuisongbao.engine.demo.widght.swipe.implments.SwipeItemMangerImpl;

import java.util.List;

public interface SwipeItemMangerInterface {

	public void openItem(int position);

	public void closeItem(int position);

	public void closeAllExcept(SwipeLayout layout);

	public List<Integer> getOpenItems();

	public List<SwipeLayout> getOpenLayouts();

	public void removeShownLayouts(SwipeLayout layout);

	public boolean isOpen(int position);

	public SwipeItemMangerImpl.Mode getMode();

	public void setMode(SwipeItemMangerImpl.Mode mode);
}
