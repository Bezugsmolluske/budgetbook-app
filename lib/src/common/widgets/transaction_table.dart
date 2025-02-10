import 'package:budgetbook_app/src/common/date/formatter.dart';
import 'package:flutter/material.dart';
import 'package:budgetbook_app/src/common/widgets/app_table_cell.dart';

class TransactionTable extends StatelessWidget {
  const TransactionTable({
    super.key,
    required this.transactions,
    required this.totalSum,
  });

  final List<Transaction> transactions;
  final String totalSum;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        color: Colors.teal.shade700,
        boxShadow: [
          BoxShadow(
            color: Colors.black26,
            blurRadius: 4,
            offset: Offset(0, 2),
          ),
        ],
      ),
      clipBehavior: Clip.hardEdge,
      child: Table(
        border: TableBorder.all(
          color: Colors.teal.shade600,
          width: 1,
        ),
        columnWidths: const {
          0: FlexColumnWidth(2), // Date
          1: FlexColumnWidth(4), // Description
          2: FlexColumnWidth(2), // Category
          3: FlexColumnWidth(2), // Amount
        },
        defaultVerticalAlignment: TableCellVerticalAlignment.middle,
        children: [
          TableRow(
            decoration: BoxDecoration(
              color: Colors.teal.shade600,
            ),
            children: const [
              AppTableCell(
                text: 'Datum',
                textColor: Colors.white,
                fontWeight: FontWeight.bold,
              ),
              AppTableCell(
                text: 'Beschreibung',
                textColor: Colors.white,
                fontWeight: FontWeight.bold,
              ),
              AppTableCell(
                text: 'Kategorie',
                textColor: Colors.white,
                fontWeight: FontWeight.bold,
              ),
              AppTableCell(
                text: 'Betrag',
                alignment: Alignment.centerRight,
                textColor: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ],
          ),
          ...transactions.map((transaction) => TableRow(
                decoration: BoxDecoration(
                  color: Colors.grey.shade300,
                ),
                children: [
                  AppTableCell(
                    text: formatDate(transaction.date),
                    textColor: Colors.black,
                  ),
                  AppTableCell(
                    text: transaction.description,
                    textColor: Colors.black,
                  ),
                  AppTableCell(
                    text: transaction.category,
                    textColor: Colors.black,
                  ),
                  AppTableCell(
                    text: transaction.amount,
                    alignment: Alignment.centerRight,
                    textColor: transaction.amount.startsWith('-')
                        ? Colors.red.shade900
                        : Colors.green.shade900,
                    fontWeight: FontWeight.w600,
                  ),
                ],
              )),
          TableRow(
            decoration: BoxDecoration(
              color: Colors.grey.shade300,
            ),
            children: [
              AppTableCell(
                text: 'Gesamt',
                textColor: Colors.black,
                fontWeight: FontWeight.w800,
              ),
              const AppTableCell(
                text: '',
                textColor: Colors.black,
              ),
              const AppTableCell(
                text: '',
                textColor: Colors.black,
              ),
              AppTableCell(
                text: totalSum,
                alignment: Alignment.centerRight,
                textColor: totalSum.startsWith('-')
                    ? Colors.red.shade900
                    : Colors.green.shade900,
                fontWeight: FontWeight.w800,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class Transaction {
  final String date;
  final String description;
  final String category;
  final String amount;

  const Transaction({
    required this.date,
    required this.description,
    required this.category,
    required this.amount,
  });
}
