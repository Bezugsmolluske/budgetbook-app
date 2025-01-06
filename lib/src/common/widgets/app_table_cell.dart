import 'package:flutter/material.dart';

class AppTableCell extends StatelessWidget {
  const AppTableCell({
    super.key,
    required this.text,
    this.alignment = Alignment.centerLeft,
    this.textColor = Colors.white,
    this.fontWeight = FontWeight.normal,
  });

  final String text;
  final Alignment alignment;
  final Color? textColor;
  final FontWeight fontWeight;

  @override
  Widget build(BuildContext context) {
    return TableCell(
      verticalAlignment: TableCellVerticalAlignment.middle,
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Container(
            alignment: alignment,
            child: Text(
              text,
              style: TextStyle(
                color: textColor,
                fontWeight: fontWeight,
              ),
            )),
      ),
    );
  }
}
