import 'package:budgetbook_app/src/common/date/months.dart';
import 'package:budgetbook_app/src/common/widgets/app_table_cell.dart';
import 'package:budgetbook_app/src/overview/model/yearly_overview.dart';
import 'package:flutter/material.dart';

class YearlyOverviewTable extends StatelessWidget {
  const YearlyOverviewTable({
    super.key,
    required this.yearlyOverview,
  });

  final YearlyOverview yearlyOverview;

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
          columnWidths: {
            0: FlexColumnWidth(),
            1: FlexColumnWidth(),
            2: FlexColumnWidth(),
            3: FlexColumnWidth(),
          },
          defaultVerticalAlignment: TableCellVerticalAlignment.middle,
          children: [
            TableRow(
              decoration: BoxDecoration(
                color: Colors.teal.shade600,
              ),
              children: [
                AppTableCell(
                  text: 'Monat',
                  textColor: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
                AppTableCell(
                  text: 'Ausgaben',
                  alignment: Alignment.centerRight,
                  textColor: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
                AppTableCell(
                  text: 'Einnahmen',
                  alignment: Alignment.centerRight,
                  textColor: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
                AppTableCell(
                  text: 'Gesamt',
                  alignment: Alignment.centerRight,
                  textColor: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ],
            ),
            ...yearlyOverview.monthlyOverviews
                .map((monthlyOverview) => TableRow(
                      decoration: BoxDecoration(
                        color: Colors.grey.shade300,
                      ),
                      children: [
                        AppTableCell(
                          text: months[monthlyOverview.month - 1],
                          textColor: Colors.black,
                          fontWeight: FontWeight.w600,
                        ),
                        AppTableCell(
                          text: monthlyOverview.expenses,
                          alignment: Alignment.centerRight,
                          textColor: Colors.red.shade900,
                        ),
                        AppTableCell(
                          text: monthlyOverview.incomes,
                          alignment: Alignment.centerRight,
                          textColor: Colors.green.shade900,
                        ),
                        AppTableCell(
                          text: monthlyOverview.sum,
                          alignment: Alignment.centerRight,
                          textColor: monthlyOverview.sum.startsWith('-')
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
                AppTableCell(
                  text: yearlyOverview.expensesSum,
                  alignment: Alignment.centerRight,
                  textColor: Colors.red.shade900,
                  fontWeight: FontWeight.w800,
                ),
                AppTableCell(
                  text: yearlyOverview.incomesSum,
                  alignment: Alignment.centerRight,
                  textColor: Colors.green.shade900,
                  fontWeight: FontWeight.w800,
                ),
                AppTableCell(
                  text: yearlyOverview.sum,
                  alignment: Alignment.centerRight,
                  textColor: yearlyOverview.sum.startsWith('-')
                      ? Colors.red.shade900
                      : Colors.green.shade900,
                  fontWeight: FontWeight.w800,
                ),
              ],
            )
          ]),
    );
  }
}
