import 'package:budgetbook_app/src/expenses/model/expense.dart';
import 'package:json_annotation/json_annotation.dart';

part 'expenses.g.dart';

@JsonSerializable(explicitToJson: true)
class Expenses {
  final String sum;
  final List<Expense> expenses;

  Expenses(this.sum, this.expenses);

  factory Expenses.fromJson(Map<String, dynamic> json) =>
      _$ExpensesFromJson(json);

  Map<String, dynamic> toJson() => _$ExpensesToJson(this);
}
