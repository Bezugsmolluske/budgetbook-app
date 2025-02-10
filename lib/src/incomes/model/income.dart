import 'package:json_annotation/json_annotation.dart';

part 'income.g.dart';

@JsonSerializable(explicitToJson: true)
class Income {
  final String id;
  final int version;
  final String date;
  final String description;
  final String category;
  final String amount;

  Income(this.id, this.version, this.date, this.description, this.category,
      this.amount);

  factory Income.fromJson(Map<String, dynamic> json) => _$IncomeFromJson(json);

  Map<String, dynamic> toJson() => _$IncomeToJson(this);
}
