String formatDate(String date) {
  final DateTime parsedDate = DateTime.parse(date);
  return '${parsedDate.day.toString().padLeft(2, '0')}.${parsedDate.month.toString().padLeft(2, '0')}.${parsedDate.year}';
}
