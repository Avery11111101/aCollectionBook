# 全能圖鑑系統 (aCollectionBook) - Paper 插件

一個專為 Paper 1.20.6 / 1.21 (26.2) 伺服器打造的高性能全能圖鑑系統插件。

## 功能特點

- **全生物與物品圖鑑**：自動分類原版生物與生存物品，具備嚴格的技術方塊黑名單過濾機制。
- **極致 UX 與 GUI 介面**：
  - 類別動態分頁與進度條 (`[■■■■■□□□□□] 50%`)。
  - 狀態篩選（全部 / 僅顯示已解鎖 / 僅顯示未解鎖）。
  - 音效回饋（解鎖音效、翻頁音效、分享音效）。
  - 點擊卡片將解鎖成就分享至聊天室與 Discord。
- **多元觸發機制**：
  - 生物：擊殺、遠程射殺、繁殖、騎乘、互動/餵食。
  - 物品：撿起、合成、採礦、熔煉、釣魚。
- **全服解鎖順序與排行榜**：記錄每項圖鑑全服第幾位解鎖，並提供 `/colltop` 排行榜 GUI。
- **異步數據庫**：支援 Flat-file 異步儲存，高並發無卡頓。

## 指令說明

- `/collection` (別名: `/mobbook`, `/pokedex`) - 打開圖鑑主選單
- `/colltop` (別名: `/mobtop`) - 查看圖鑑解鎖排行榜
- `/colladmin reload` - 重載設定檔與語言檔
- `/colladmin unlock <玩家> <項目>` - 為玩家強制解鎖特定項目
- `/colladmin resetplayer <玩家>` - 重置玩家圖鑑紀錄
- `/colladmin resetall` - 清空全服圖鑑紀錄
- `/colladmin check <玩家>` - 查詢玩家解鎖總數

## 安裝與建置步驟

1. 使用 Maven 進行打包：
   ```bash
   mvn clean package
   ```
2. 將 `target/aCollectionBook-0.1.jar` 放入伺服器的 `plugins/` 資料夾。
3. 重啟伺服器或使用 PlugMan/Paper 載入即可。
