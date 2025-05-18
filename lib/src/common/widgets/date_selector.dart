import 'package:flutter/material.dart';

class DateSelector extends StatelessWidget {
  const DateSelector({
    super.key,
    required this.selectedDate,
    required this.onDateChanged,
  });

  final DateTime selectedDate;
  final Function(DateTime) onDateChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        DropdownButton<int>(
          value: selectedDate.month,
          items: List.generate(12, (index) => index + 1)
              .map((month) => DropdownMenuItem(
                    value: month,
                    child: Text(month.toString()),
                  ))
              .toList(),
          onChanged: (value) {
            if (value != null) {
              onDateChanged(DateTime(
                selectedDate.year,
                value,
              ));
            }
          },
        ),
        const SizedBox(width: 8),
        DropdownButton<int>(
          value: selectedDate.year,
          items: List.generate(5, (index) => DateTime.now().year - 4 + index)
              .map((year) => DropdownMenuItem(
                    value: year,
                    child: Text(year.toString()),
                  ))
              .toList(),
          onChanged: (value) {
            if (value != null) {
              onDateChanged(DateTime(
                value,
                selectedDate.month,
              ));
            }
          },
        ),
      ],
    );
  }
}
