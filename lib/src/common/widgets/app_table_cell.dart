import 'package:flutter/material.dart';

class AppTableCell extends StatelessWidget {
  const AppTableCell(
      {super.key,
      required this.text,
      this.alignment = Alignment.centerLeft,
      this.color});

  final String text;
  final Alignment alignment;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    return TableCell(
      verticalAlignment: TableCellVerticalAlignment.middle,
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(
            alignment: alignment,
            child: Text(style: TextStyle(color: color), text)),
      ),
    );
  }
}
