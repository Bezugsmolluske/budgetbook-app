import 'package:json_annotation/json_annotation.dart';

part 'expense.g.dart';

@JsonSerializable(explicitToJson: true)
class Expense {
  final String id;
  final int version;
  final String date;
  final String description;
  final String category;
  final String amount;

  Expense(this.id, this.version, this.date, this.description, this.category,
      this.amount);

  factory Expense.fromJson(Map<String, dynamic> json) =>
      _$ExpenseFromJson(json);

  Map<String, dynamic> toJson() => _$ExpenseToJson(this);
}
