package com.podmev.cashsplitter.data
/*common UI state for all project
* place for global variables
* */
class UIDataState {
    enum class ToolBarState{
        TOOLBAR_STATE_HIDE_MENU, TOOLBAR_STATE_UNHIDE_MENU
    }
    companion object{
        private var toolbarState: ToolBarState = ToolBarState.TOOLBAR_STATE_UNHIDE_MENU // setting state
        fun hideMenu(){
            toolbarState = ToolBarState.TOOLBAR_STATE_HIDE_MENU
        }

        fun unhideMenu(){
            toolbarState = ToolBarState.TOOLBAR_STATE_UNHIDE_MENU
        }

        fun isMenuHidden() = toolbarState == ToolBarState.TOOLBAR_STATE_HIDE_MENU

        var useTotalTextView: Boolean = false
        var useAvailableTextView: Boolean = false
        var useNotPlannedTextView: Boolean = false
    }
}