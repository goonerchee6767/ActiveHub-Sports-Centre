# ActiveHub Sports Centre Management System

A Java console application for **ActiveHub Sports Centre**: facility booking, equipment rental, a rule-based promotion engine, payments and a daily report.

DIT2123 Object-Oriented Modelling – Group Assignment (Aug–Dec 2026), Sunway College.

---

## Requirements

- **Java JDK 8 or newer** (JDK, not only JRE, because `javac` is needed to compile)
  - Check by running `javac -version` in a terminal.
  - If it is not installed, download a free JDK from <https://adoptium.net> (choose the latest LTS version).
- No other libraries are needed. The program uses only the standard Java library.

---

## How to run

### Option 1 – Double-click (easiest)

| System | File to run |
|---|---|
| Windows | Double-click **`run.bat`** |
| macOS / Linux | Run `./run.sh` in a terminal (first time: `chmod +x run.sh`) |

The script compiles the code and starts the program.

### Option 2 – Command line (any system)

Open a terminal **inside the project folder** and run:

```
javac -d out *.java
java -cp out ActiveHubSystem
```

> Always run from the project folder, so the program finds the `data` folder.

### Option 3 – VS Code

1. Install the **Extension Pack for Java** (by Microsoft) from the Extensions panel.
2. **File → Open Folder…** and choose this project folder.
3. Open `ActiveHubSystem.java` and click **Run** above `public static void main`.
4. Type your menu choices in the terminal panel at the bottom.

### Option 4 – NetBeans / IntelliJ / Eclipse

Create a new Java project from existing sources, select this folder, and run `ActiveHubSystem` (it contains the `main` method).

---

## Using the program

```
===== ACTIVEHUB SYSTEM =====
1. Facility Booking Module
2. View Rental Catalogue
3. Create Rental Transaction
4. Apply Promotion
5. Make Payment
6. Daily Report
7. Exit
```

Type a number and press **Enter**.

- Dates use the format `yyyy-MM-dd` (e.g. `2026-11-05`).
- Times use 24-hour `HH:mm` (e.g. `09:00`, `15:30`).
- Opening hours are 08:00–22:00. Bookings can be 1–4 hours long.

### Quick demo

1. **1 → 1** Make a booking: a weekday date, `10:00`, `2` hours, type `1` (Badminton), `8` participants, press Enter to auto-assign a court, `Y` to confirm.
2. `Y` to create a transaction, `Y` to add equipment, add `E001` × `3`, then `0` to finish.
3. The bill shows all eligible promotions and applies the one with the biggest saving.
4. Back in the main menu, choose **5** to pay, then **6** to see the daily report.

---

## Promotions

| Code | Name | Rule |
|---|---|---|
| A | Off-Peak Saver | 15% off facility charges for bookings starting Mon–Fri, from 09:00 to before 16:00 |
| B | Team Booking Reward | RM5 per participant for 8+ participants (max RM60, never more than the facility charge) |
| C | Equipment Bundle Discount | RM20 off with at least 1 facility booking and 3+ equipment units |

Only the promotion with the **greatest saving** is applied. Ties go to the lower code (A, then B, then C). A 10% service charge is added after the discount.

---

## Data files

All data is saved automatically in the `data` folder as plain text (one record per line, fields separated by `|`):

| File | Contents |
|---|---|
| `catalogue.txt` | Rental items |
| `facilities.txt` | Courts |
| `bookings.txt` | Facility bookings |
| `transactions.txt` | Rental transactions |
| `transaction_items.txt` | Items in each transaction |
| `payments.txt` | Payments |

The included files contain sample data. To start with an empty system, delete `bookings.txt`, `transactions.txt`, `transaction_items.txt` and `payments.txt`. The catalogue and courts are re-created automatically if their files are missing.

---

## Project structure

| Area | Classes |
|---|---|
| Main / control | `ActiveHubSystem`, `InputHelper`, `DataStore`, `Money` |
| Booking | `Customer`, `Facility`, `FacilityType`, `Booking`, `BookingStatus`, `BookingManager` |
| Catalogue | `RentalItem` (abstract), `EquipmentItem`, `FacilityPackage`, `AddOnItem`, `ItemCategory`, `Catalogue` |
| Transactions | `RentalTransaction`, `TransactionItem`, `TransactionStatus`, `TransactionManager` |
| Promotions | `Promotion` (abstract), `OffPeakSaverPromotion`, `TeamBookingPromotion`, `EquipmentBundlePromotion`, `PromotionEngine`, `PromotionResult` |
| Payments | `Payment` (abstract), `CashPayment`, `CardPayment`, `EWalletPayment` |
| Reporting | `ReportGenerator` |
| Interface | `Storable` |

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `'javac' is not recognized` / `command not found` | Install a JDK (see Requirements) and reopen the terminal. |
| `Could not find or load main class ActiveHubSystem` | Run the commands from inside the project folder, and compile first. |
| Data from a previous run is missing | Make sure you run from the project folder (the program reads `./data`). |
