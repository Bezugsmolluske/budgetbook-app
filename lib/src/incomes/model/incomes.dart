import 'package:budgetbook_app/src/incomes/model/income.dart';
import 'package:json_annotation/json_annotation.dart';

part 'incomes.g.dart';

@JsonSerializable(explicitToJson: true)
class Incomes {
  final String amount;
  final List<Income> incomes;

  Incomes(this.amount, this.incomes);

  factory Incomes.fromJson(Map<String, dynamic> json) =>
      _$IncomesFromJson(json);

  Map<String, dynamic> toJson() => _$IncomesToJson(this);
}
