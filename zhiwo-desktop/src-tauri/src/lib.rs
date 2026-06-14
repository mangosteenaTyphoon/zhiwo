// 自定义窗口移动命令
#[tauri::command]
fn drag_move(window: tauri::WebviewWindow, x: f64, y: f64) -> Result<(), String> {
    window
        .set_position(tauri::Position::Logical(tauri::LogicalPosition::new(x, y)))
        .map_err(|e| e.to_string())
}

// 获取窗口位置（逻辑坐标，跨屏安全）
#[tauri::command]
fn get_pos(window: tauri::WebviewWindow) -> Result<(f64, f64), String> {
    let pos = window.outer_position().map_err(|e| e.to_string())?;
    let scale = window.scale_factor().map_err(|e| e.to_string())?;
    Ok((pos.x as f64 / scale, pos.y as f64 / scale))
}

// 窗口控制
#[tauri::command]
fn win_close(window: tauri::WebviewWindow) {
    let _ = window.close();
}

#[tauri::command]
fn win_minimize(window: tauri::WebviewWindow) {
    let _ = window.minimize();
}

#[tauri::command]
fn win_toggle_maximize(window: tauri::WebviewWindow) {
    if let Ok(m) = window.is_maximized() {
        if m { let _ = window.unmaximize(); }
        else { let _ = window.maximize(); }
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .invoke_handler(tauri::generate_handler![
            drag_move,
            get_pos,
            win_close,
            win_minimize,
            win_toggle_maximize
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
