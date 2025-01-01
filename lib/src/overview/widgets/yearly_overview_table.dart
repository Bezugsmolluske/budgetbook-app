import 'package:budgetbook_app/src/common/date/months.dart';
import 'package:budgetbook_app/src/common/widgets/app_table_cell.dart';
import 'package:budgetbook_app/src/overview/model/monthly_overview.dart';
import 'package:flutter/material.dart';

class YearlyOverviewTable extends StatelessWidget {
  const YearlyOverviewTable({
    super.key,
    required this.monthlyOverviews,
  });

  final List<MonthlyOverview> monthlyOverviews;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        color: Colors.blueGrey,
        boxShadow: [
          BoxShadow(color: Colors.white, spreadRadius: 1),
        ],
      ),
      clipBehavior: Clip.hardEdge,
      child: Table(
          border: TableBorder.all(color: Colors.white),
          columnWidths: {
            0: FlexColumnWidth(),
            1: FlexColumnWidth(),
            2: FlexColumnWidth(),
            3: FlexColumnWidth(),
          },
          defaultVerticalAlignment: TableCellVerticalAlignment.middle,
          children: [
            TableRow(
              decoration: const BoxDecoration(
                color: Colors.lightBlue,
              ),
              children: [
                AppTableCell(text: 'Monat', color: Colors.black),
                AppTableCell(
                    text: 'Ausgaben',
                    alignment: Alignment.centerRight,
                    color: Colors.black),
                AppTableCell(
                    text: 'Einnahmen',
                    alignment: Alignment.centerRight,
                    color: Colors.black),
                AppTableCell(
                    text: 'Gesamt',
                    alignment: Alignment.centerRight,
                    color: Colors.black),
              ],
            ),
            ...monthlyOverviews.map((monthlyOverview) => TableRow(
                  decoration: const BoxDecoration(
                    color: Colors.blueGrey,
                  ),
                  children: [
                    AppTableCell(text: months[monthlyOverview.month - 1]),
                    AppTableCell(
                        text: monthlyOverview.expenses,
                        alignment: Alignment.centerRight),
                    AppTableCell(
                        text: monthlyOverview.incomes,
                        alignment: Alignment.centerRight),
                    AppTableCell(
                        text: monthlyOverview.sum,
                        alignment: Alignment.centerRight),
                  ],
                ))
          ]),
    );
  }
}
